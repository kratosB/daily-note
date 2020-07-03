package tool;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * Created on 2020/4/17.
 *
 * @author zhiqiang bao
 */
@Slf4j
public class PdfToJpg {

    private ZipTool zipTool;

    public PdfToJpg() {
        zipTool = new ZipTool();
    }

    public static void main(String[] args) {
    }

    public String pdfConvert(MultipartFile file) {
        // 1. 先把zip文件保存为临时文件
        String tempFilePath = saveTempFile(file);
        // 2. 解压文件获取"文件名-byte数组"键值对
        File tempFile = new File(tempFilePath);
        String message = "pdf转图片失败";
        Map<String, byte[]> fileMap = getBytesMapFromZipFile(tempFile, message);
        // 成功之后, 删除临时文件
        tempFile.delete();
        // 3. 循环转换pdf
        // 3.1 创建临时文件夹
        String tempImgFolderPath = "/temp/" + LocalDate.now().toString() + "/img" + LocalTime.now().toSecondOfDay() + "/";
        File tempImgFoler = new File(tempImgFolderPath);
        if (!tempImgFoler.exists()) {
            boolean mkdirs = tempImgFoler.mkdirs();
            if (!mkdirs) {
                log.error("pdf转图片失败, 创建临时文件夹失败, tempImgFolderPath = {}", tempImgFolderPath);
                throw new RuntimeException("pdf转图片失败, 创建临时文件夹失败");
            }
        }
        // 3.2 循环转换
        fileMap.keySet().parallelStream().forEach(fileName -> {
            String pdfU = ".PDF";
            String pdfL = ".pdf";
            if (!fileName.contains(pdfU) && !fileName.contains(pdfL)) {
                log.info("pdf转图片失败, 传入文件不是pdf格式, fileName = {}", fileName);
                throw new RuntimeException("pdf转图片失败, 传入文件不是pdf格式");
            }
            byte[] fileBytes = fileMap.get(fileName);
            try {
                PDDocument doc = PDDocument.load(fileBytes);
                PDFRenderer renderer = new PDFRenderer(doc);
                int pageCount = doc.getNumberOfPages();
                for (int index = 0; index < pageCount; index++) {
                    String tempImgPath = tempImgFolderPath + fileName + "_" + (index + 1) + ".jpg";
                    log.info("pdf转图片, fileName = {}, index = {}", tempImgPath, index);
                    BufferedImage image = renderer.renderImageWithDPI(index, 144);
                    ImageIO.write(image, "jpg", new File(tempImgPath));
                }
                doc.close();
            } catch (IOException e) {
                log.error("pdf转图片失败, pdf解析失败, tempImgFolderPath = {}, e = {}", tempImgFolderPath, e.toString());
                throw new RuntimeException("pdf解析失败");
            }
        });
        // 4. 压缩图片到输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        zipTool.toZip(tempImgFolderPath, outputStream, true);
        // 成功之后，删除临时文件
        if (tempImgFoler.exists()) {
            deleteTempFiles(tempImgFoler);
        }
        // 5. 写文件
        String fileName = "img";
        String postfix = ".zip";
        return saveFile(fileName, outputStream, message, postfix);
    }

    private String saveTempFile(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String zip = ".zip";
        if (!originalFilename.contains(zip)) {
            log.info("发票识别失败, 上传的文件不是zip文件, fileName = {}", originalFilename);
            throw new RuntimeException("发票识别失败, 上传的文件不是zip文件");
        }
        byte[] bytes;
        try {
            bytes = multipartFile.getBytes();
        } catch (IOException e) {
            log.error("发票识别失败, 从multipartFile中获取bytes失败, fileName = {}", originalFilename);
            throw new RuntimeException("发票识别失败, 文件读取错误");
        }
        String tempFilePath = "/temp/" + LocalDate.now().toString() + "/" + originalFilename;
        try {
            InputStream is = new ByteArrayInputStream(bytes);
            FileUtils.copyInputStreamToFile(is, new File(tempFilePath));
            is.close();
        } catch (Exception e) {
            log.error("发票识别失败, 保存临时文件失败, tempFilePath = {}", tempFilePath);
            throw new RuntimeException("发票识别失败, 文件读取错误");
        }
        return tempFilePath;
    }

    private Map<String, byte[]> getBytesMapFromZipFile(File tempFile, String message) {
        Map<String, byte[]> fileMap;
        try {
            fileMap = zipTool.getBytesListFromZipFile(tempFile);
        } catch (Exception e) {
            throw new RuntimeException(message + ", " + e.toString());
        }
        return fileMap;
    }

    private void deleteTempFiles(File file) {
        boolean directory = file.isDirectory();
        if (directory) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                deleteTempFiles(listFile);
            }
        }
        file.delete();
    }

    private String saveFile(String fileName, ByteArrayOutputStream outputStream, String message, String postfix) {
        String path = "/" + LocalDate.now().toString() + "/" + fileName + "_" + LocalTime.now().toSecondOfDay() + postfix;
        try {
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
            FileUtils.copyInputStreamToFile(is, new File(path));
            outputStream.close();
            is.close();
        } catch (Exception e) {
            log.error("{}, 保存文件到本地失败, 识别数据可以从日志中找到, e = {}", message, e.toString());
            throw new RuntimeException(message + ", 保存文件失败");
        }
        return "/" + path.substring(path.indexOf("/data/iqunxing/download") + 23);
    }

}
