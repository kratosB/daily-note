package com.iqunxing.common.api;

import com.google.common.base.Charsets;
import com.iqunxing.springtime.modules.base.exception.NotFoundException;
import com.iqunxing.springtime.modules.base.exception.ServiceException;
import com.mchange.v1.io.InputStreamUtils;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created on 2019/10/18.
 *
 * @author zhiqiang bao
 */
@Slf4j
@RestController
public class UploadDownloadDemo {

    /**
     * 上传附件，rest template转发
     **/
    @PostMapping(value = "/contract/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Boolean> uploadAttachment(@RequestPart MultipartFile[] files, @RequestParam String id) throws Exception {
        return uploadAttachment1(files, id);
    }

    private Map<String, Boolean> uploadAttachment1(MultipartFile[] files, String id) throws Exception {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.set("id", id);
        for (MultipartFile file : files) {
            HttpHeaders fileHeader = new HttpHeaders();
            fileHeader.setContentType(MediaType.TEXT_PLAIN);
            fileHeader.setContentDispositionFormData("file", URLEncoder.encode(file.getOriginalFilename(), Charsets.UTF_8.name()));
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileHeader);
            // form.add多个file可以自动转化为数组
            form.add("file", fileEntity);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);
        String url = "http://localhost:8080/api/attachments/upload";
        ResponseEntity<HashMap> hashMapResponseEntity = new RestTemplate().postForEntity(url, entity, HashMap.class);
        return hashMapResponseEntity.getBody();
    }

    /**
     * 下载附件，rest template转发
     **/
    @GetMapping(value = "/contract/attachments/download")
    public void downloadAttachment(@RequestParam String id, HttpServletResponse response) throws Exception {
        byte[] bytes = downloadAttachment1(id);
        ServletOutputStream outputStream = response.getOutputStream();
        String fileName = URLEncoder.encode(id + ".zip", Charsets.UTF_8.name());
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        IOUtils.write(bytes, outputStream);
    }

    private byte[] downloadAttachment1(String id) throws Exception {
        String url = "http://localhost:8080/api/attachments/download?id=" + id;
        // 因为那个接口返回void，然后回写流，所以这边用resource去拿inputStream
        ResponseEntity<Resource> forEntity = new RestTemplate().getForEntity(url, Resource.class);
        InputStream inputStream = forEntity.getBody().getInputStream();
        return InputStreamUtils.getBytes(inputStream);
    }

    @ApiOperation("上传文件")
    @PostMapping(value = "api/attachment/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestPart MultipartFile file, @ModelAttribute UploadFileReq uploadFileReq) throws Exception {
        // 这个例子中uploadFileReq其实没用，只是为了说明，file和参数一起传应该怎么写
        final UploadFileReq fileReq = Optional.ofNullable(uploadFileReq).orElse(new UploadFileReq());
        if (Optional.ofNullable(file).isPresent()) {
            fileReq.setIs(file.getInputStream());
            String fileName = file.getOriginalFilename();
            fileName = URLDecoder.decode(fileName, Charsets.UTF_8.name());
            fileReq.setFileName(fileName);
            FileUtils.copyInputStreamToFile(fileReq.getIs(), new File(fileReq.getFileName()));
        } else {
            throw new ServiceException("上传文件不能为空");
        }
    }

    /**
     * 批量上传swagger不能测试（不支持选中多个文件），要用postman测试
     */
    @ApiOperation("批量上传文件")
    @PostMapping(value = "api/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Boolean> upload(@RequestPart MultipartFile[] files, @ModelAttribute UploadFileReq uploadFileReq) {
        Map<String, Boolean> result = new HashMap<>(16);
        final UploadFileReq fileReq = Optional.ofNullable(uploadFileReq).orElse(new UploadFileReq());
        if (Optional.ofNullable(files).isPresent()) {
            for (MultipartFile file : files) {
                try {
                    uploadDetail(file, fileReq);
                    result.put(file.getOriginalFilename(), true);
                } catch (Exception e) {
                    result.put(file.getOriginalFilename(), false);
                }
            }
        } else {
            throw new ServiceException("上传文件不能为空");
        }
        return result;
    }

    private void uploadDetail(MultipartFile file, UploadFileReq uploadFileReq) throws Exception {
        uploadFileReq.setIs(file.getInputStream());
        String fileName = file.getOriginalFilename();
        fileName = URLDecoder.decode(fileName, Charsets.UTF_8.name());
        uploadFileReq.setFileName(fileName);
        FileUtils.copyInputStreamToFile(uploadFileReq.getIs(), new File(uploadFileReq.getFileName()));
    }

    @ApiOperation("下载文件")
    @GetMapping("api/common/v1/attachment/download/{id}")
    public void download(@ApiParam("文件id") @PathVariable("id") Long id, HttpServletResponse response) throws Exception {
        byte[] bytes = Optional.ofNullable(get(id)).orElseThrow(() -> new NotFoundException("文件未找到"));
        OutputStream os = response.getOutputStream();
        String fileName = URLEncoder.encode("FileName", Charsets.UTF_8.name());
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        IOUtils.write(bytes, os);
    }

    @HystrixCommand
    @ApiOperation("批量下载文件（zip包）")
    @GetMapping("api/common/v1/attachments/download")
    public void download(@ModelAttribute DownloadReq downloadReq, HttpServletResponse response) throws Exception {
        byte[] byTypeAndId = getByTypeAndId(downloadReq.getId());
        OutputStream os = response.getOutputStream();
        String fileName = URLEncoder.encode(downloadReq.getId() + ".zip", Charsets.UTF_8.name());
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        IOUtils.write(byTypeAndId, os);
    }

    private byte[] get(Long id) throws Exception {
        // 假的根据id查找对应的attachmentVo
        String dir = new Attachment().getDir();
        if (StringUtils.isNotBlank(dir)) {
            return FileUtils.readFileToByteArray(new File(dir));
        }
        return null;
    }

    private byte[] getByTypeAndId(Long id) throws Exception {
        // 假的根据id查找对应的attachmentVo
        List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(new Attachment());
        // 用来计数，以免文件名重复，导致压缩报错
        Map<String, Integer> fileNameMap = attachmentList.stream().map(Attachment::getFileName).distinct()
                .collect(Collectors.toMap(name -> name, name -> 1));
        File zipFile = new File(id + "-temp.zip");
        // 定义文件输入流
        FileInputStream input;
        // 声明压缩流对象
        ZipOutputStream zipOut;
        int temp;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            for (Attachment attachment : attachmentList) {
                String dir = attachment.getDir();
                if (StringUtils.isNotBlank(dir)) {
                    input = new FileInputStream(new File(dir));
                    // 如果文件名重复，zip打包会报错，所以这里要处理重复文件名
                    String fileName = attachment.getFileName();
                    Integer fileNameCount = fileNameMap.get(fileName);
                    if (fileNameCount > 1) {
                        int lastIndexOfDot = fileName.lastIndexOf(".");
                        fileName = fileName.substring(0, lastIndexOfDot) + "（" + fileNameCount + "）"
                                + fileName.substring(lastIndexOfDot);
                    }
                    // 设置ZipEntry对象
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    fileNameMap.put(fileName, ++fileNameCount);
                    // 读取内容
                    while ((temp = input.read()) != -1) {
                        // 压缩输出
                        zipOut.write(temp);
                    }
                    // 关闭输入流
                    IOUtils.closeQuietly(input);
                } else {
                    log.error("根据类型和编号下载文件，压缩文件出错，没找到对应fileName={}的文件，attachmentId={}", attachment.getFileName(),
                            attachment.getId());
                    zipFile.delete();
                    throw new ServiceException("压缩文件出错，未找到对应fileName=" + attachment.getFileName() + "的文件");
                }
            }
            // 关闭输出流
            IOUtils.closeQuietly(zipOut);
            return FileUtils.readFileToByteArray(zipFile);
        } catch (FileNotFoundException e) {
            log.error("压缩文件出错，id={}，e={}", id, e.getMessage());
            throw new ServiceException("根据类型和编号下载文件，压缩文件出错");
        } finally {
            zipFile.delete();
        }
    }

}

@Data
@ApiModel("上传文件请求")
class UploadFileReq {

    @ApiModelProperty("文件类型")
    private String objectType;

    @ApiModelProperty("文件类型")
    private String objectId;

    @ApiModelProperty("文件属性")
    private String props;

    @ApiModelProperty(hidden = true)
    private InputStream is;

    @ApiModelProperty(hidden = true)
    private String fileName;

}

@Data
class Attachment {

    @Id
    @Column(name = "pkey")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "storage_name")
    private String storageName;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "dir")
    private String dir;

}

@Data
class DownloadReq {

    @NotBlank
    private long id;

}
