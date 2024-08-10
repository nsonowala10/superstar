package com.quotes.premium.config;

import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;

public class AnnualCheckUpConfig {
    public static void fetchAnnualCheckUp(String filePath) {

        ClassLoader classLoader = DynamicConfigurations.class.getClassLoader();
        try (InputStream file = classLoader.getResourceAsStream(filePath);
             Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row waitingPeriodRow = sheet.getRow(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                for (int cellIndex = 1; cellIndex < row.getLastCellNum(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    if (cell == null) continue;

                    String policyType = row.getCell(0).getStringCellValue().trim();
                    String sumInsured = null;
                    switch(waitingPeriodRow.getCell(cellIndex).getCellType()){
                        case NUMERIC -> sumInsured =String.valueOf((long)waitingPeriodRow.getCell(cellIndex).getNumericCellValue());
                        case STRING -> sumInsured =waitingPeriodRow.getCell(cellIndex).getStringCellValue();
                    }
                    String key = policyType.toLowerCase() + "#" + sumInsured;

                    DynamicConfigurations.annualHealthCheck.put(key, cell.getNumericCellValue());
                    System.out.println(key + " " + cell.getNumericCellValue());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Double getAnnualCheckUp(final String policyType, final String sumInsured) {
        String key = policyType + "#" + sumInsured;
        return DynamicConfigurations.annualHealthCheck.get(key);
    }
}
