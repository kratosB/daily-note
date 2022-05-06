package tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.VatInvoiceOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.VatInvoiceOCRResponse;

import net.coobird.thumbnailator.Thumbnails;
import sun.misc.BASE64Encoder;

/**
 * Created on 2020/4/14.
 *
 * @author zhiqiang bao
 */
public class OcrTool {

    public static void main(String[] args) {
        StringBuilder errorMessage = new StringBuilder();
        // 1. 读所有文件路径
        String path = "C:\\Users\\admin\\Desktop\\out\\发票2\\";
        List<String> filePathList = getFilePath(new File(path));
        // 2. 创建excel
        Workbook workBook = new XSSFWorkbook();
        Sheet sheet = workBook.createSheet("ocr结果");
        initExcel(sheet.createRow(0));
        // 文件容量限制，如果大于这个，需要压缩
        int maxlength = 7396760;
        for (int i = 0; i < filePathList.size(); i++) {
            String filePath = filePathList.get(i);
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
            // 3. 获取base64
            try {
                String encodedValue = getEncodedValue(filePath, maxlength);
                try {
                    // 4. 发票识别，数据解析，文件写入
                    String ocrValue = ocr(encodedValue);
                    System.out.println(fileName + " = " + ocrValue);
                    // 5. 提取数据
                    HashMap<Integer, String> map = getMap(ocrValue);
                    // 6. 写excel
                    Row row = sheet.createRow(i + 1);
                    writeRow(map, fileName, row);
                } catch (Exception e) {
                    errorMessage.append("发生错误，fileName = ").append(fileName).append(", encodeValue.length = ")
                            .append(encodedValue.length()).append(", e = ").append(e.getMessage()).append("\n");
                    Row row = sheet.createRow(i + 1);
                    Cell cell0 = row.createCell(0, 1);
                    cell0.setCellValue(fileName);
                }
            } catch (Exception e) {
                errorMessage.append("文件名为《").append(fileName).append("》的文件读取错误，错误 = （").append(e.getMessage()).append(")")
                        .append("\n");
            }
        }
        // 7. 刷流
        try {
            String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            OutputStream out = new FileOutputStream("C:\\Users\\admin\\Desktop\\Ocr_Result_" + date + ".xlsx");
            workBook.write(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            errorMessage.append("错误 = （").append(e.getMessage()).append(")");
        }
        System.out.println(errorMessage);
    }

    private static List<String> getFilePath(File file) {
        ArrayList<String> filePathList = Lists.newArrayList();
        boolean directory = file.isDirectory();
        if (directory) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                List<String> subFilePathList = getFilePath(listFile);
                filePathList.addAll(subFilePathList);
            }
        } else {
            filePathList.add(file.getAbsolutePath());
        }
        return filePathList;
    }

    private static String getEncodedValue(String filePath, int maxLength) throws Exception {
        InputStream fileInputStream = new FileInputStream(filePath);
        int available = fileInputStream.available();
        byte[] bytes = new byte[available];
        fileInputStream.read(bytes);
        fileInputStream.close();
        return encode(bytes, maxLength);
    }

    private static String encode(byte[] bytes, int maxLength) throws Exception {
        String encode = new BASE64Encoder().encode(bytes);
        if (encode.length() > maxLength) {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(inputStream).scale(1f).outputQuality(0.95f).toOutputStream(outputStream);
            byte[] newBytes = outputStream.toByteArray();
            return encode(newBytes, maxLength);
        } else {
            return encode;
        }
    }

    private static String ocr(String encodeValue) throws Exception {
        Credential cred = new Credential("", "");
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ocr.ap-shanghai.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        OcrClient client = new OcrClient(cred, "ap-shanghai", clientProfile);
        String params = "{\"ImageBase64\":\"" + encodeValue + "\"}";
        VatInvoiceOCRRequest req = VatInvoiceOCRRequest.fromJsonString(params, VatInvoiceOCRRequest.class);
        VatInvoiceOCRResponse resp = client.VatInvoiceOCR(req);
        return VatInvoiceOCRRequest.toJsonString(resp);
    }

    private static HashMap<Integer, String> getMap(String resultString) {
        JsonNode jsonNode = JsonMapper.nonEmptyMapper().fromJson(resultString, JsonNode.class);
        JsonNode vatInvoiceInfos = jsonNode.path("VatInvoiceInfos");
        HashMap<Integer, String> result = new HashMap<>(16);
        String number = "发票号码";
        String serialNo = "发票代码";
        String sellerName = "销售方名称";
        String buyerName = "购买方名称";
        String date = "开票日期";
        String amount = "小写金额";
        String amountWithoutTax = "合计金额";
        String checkNo = "校验码";
        String comment = "备注";
        vatInvoiceInfos.forEach(node -> {
            String name = node.get("Name").asText();
            if (StringUtils.equals(name, number)) {
                result.put(1, node.get("Value").asText().replace("No", ""));
            } else if (StringUtils.equals(name, serialNo)) {
                result.put(2, node.get("Value").asText());
            } else if (StringUtils.equals(name, sellerName)) {
                result.put(3, node.get("Value").asText());
            } else if (StringUtils.equals(name, buyerName)) {
                result.put(4, node.get("Value").asText());
            } else if (StringUtils.equals(name, date)) {
                result.put(5, node.get("Value").asText());
            } else if (StringUtils.equals(name, amount)) {
                result.put(6, node.get("Value").asText().replace("¥", ""));
            } else if (StringUtils.equals(name, amountWithoutTax)) {
                result.put(7, node.get("Value").asText().replace("¥", ""));
            } else if (StringUtils.equals(name, checkNo)) {
                result.put(8, node.get("Value").asText());
            } else if (StringUtils.equals(name, comment)) {
                result.put(9, node.get("Value").asText());
            }
        });
        return result;
    }

    private static void writeRow(HashMap<Integer, String> map, String fileName, Row row) {
        Cell cell0 = row.createCell(0, 1);
        cell0.setCellValue(fileName);
        if (map != null) {
            int count = 9;
            for (int i = 1; i <= count; i++) {
                Cell cell = row.createCell(i, 1);
                cell.setCellValue(map.get(i));
            }
        }
    }

    private static void initExcel(Row row) {
        Cell cell0 = row.createCell(0, 1);
        Cell cell1 = row.createCell(1, 1);
        Cell cell2 = row.createCell(2, 1);
        Cell cell3 = row.createCell(3, 1);
        Cell cell4 = row.createCell(4, 1);
        Cell cell5 = row.createCell(5, 1);
        Cell cell6 = row.createCell(6, 1);
        Cell cell7 = row.createCell(7, 1);
        Cell cell8 = row.createCell(8, 1);
        Cell cell9 = row.createCell(9, 1);
        Cell cell10 = row.createCell(10, 1);
        cell0.setCellValue("文件名称");
        cell1.setCellValue("发票号码");
        cell2.setCellValue("发票代码");
        cell3.setCellValue("销售方名称");
        cell4.setCellValue("购买方名称");
        cell5.setCellValue("开票日期");
        cell6.setCellValue("小写金额");
        cell7.setCellValue("不含税金额");
        cell8.setCellValue("校验码");
        cell9.setCellValue("备注");
        cell10.setCellValue("验真结果");
    }

}
