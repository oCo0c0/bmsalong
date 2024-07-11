package com.along.utils;

import org.springframework.boot.test.context.SpringBootTest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Desc
 * @Author wangtianlong
 * @Date 2024/7/11
 */
@SpringBootTest
public class ExcelExportTest {

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\coco\\Desktop\\test.xlsx";
        String imagePath = "C:\\Users\\coco\\Desktop\\284859.jpg"; // 本地图片路径

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 0));

            try (FileInputStream fis = new FileInputStream(imagePath)) {
                byte[] bytes = IOUtils.toByteArray(fis);
                int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);

                Drawing<?> drawing = sheet.createDrawingPatriarch();
                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();

                anchor.setCol1(0); // 起始列
                anchor.setRow1(0); // 起始行
                anchor.setCol2(1); // 结束列
                anchor.setRow2(3); // 结束行
                anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);

                Picture picture = drawing.createPicture(anchor, pictureIdx);

                picture.resize(1.0);

                try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                    workbook.write(fos);
                }
                System.out.println("Excel文件导出成功：" + excelFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
