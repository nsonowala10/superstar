package com.quotes.premium.config;

import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;

public class PedWaitingConfig {

    public static void fetchReductionOfPEDWaiting(final String filePath) {
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

                    String ageBand = row.getCell(0).getStringCellValue().trim();
                    String waitingPeriod = waitingPeriodRow.getCell(cellIndex).getStringCellValue();
                    String key = ageBand + "#" + waitingPeriod;

                    DynamicConfigurations.reductionOfPEWaitingPeriod.put(key, cell.getNumericCellValue());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Double getPedValue(int age, String waitingPeriodInMonths) {

        String ageBand;
        if (age <= 35) {
            ageBand = "LTE 35";
        } else if (age <= 45) {
            ageBand = "36-45";
        } else if (age <= 50) {
            ageBand = "46-50";
        } else if (age <= 55) {
            ageBand = "51-55";
        } else if (age <= 60) {
            ageBand = "56-60";
        } else if (age <= 65) {
            ageBand = "61-65";
        } else {
            ageBand = "GT 65";
        }

        String key = ageBand + "#" + waitingPeriodInMonths;
        return DynamicConfigurations.reductionOfPEWaitingPeriod.get(key);
    }
}
