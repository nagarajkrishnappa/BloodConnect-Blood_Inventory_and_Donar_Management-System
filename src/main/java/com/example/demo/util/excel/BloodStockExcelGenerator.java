package com.example.demo.util.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.example.demo.dto.response.BloodStockResponse;

public class BloodStockExcelGenerator {

    private BloodStockExcelGenerator() {
    }

    public static ByteArrayInputStream generate(List<BloodStockResponse> stocks) {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("Blood Stock");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("Blood Group");
            header.createCell(1).setCellValue("Units Available");

            int rowNum = 1;

            for (BloodStockResponse stock : stocks) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(stock.getBloodGroup() != null ? stock.getBloodGroup().getValue() : "N/A");

                row.createCell(1).setCellValue(stock.getUnits() != null ? stock.getUnits() : 0);

            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {

            throw new RuntimeException("Failed to generate Excel file.", e);

        }
    }
}
