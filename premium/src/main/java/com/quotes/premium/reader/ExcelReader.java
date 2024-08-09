package com.quotes.premium.reader;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class ExcelReader {

    public void preparePremium(Map<String,Double> valueMap , String filePath){
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(file)) {
            Map<Integer, String> headerMap = new HashMap<>();
            Sheet sheet = workbook.getSheetAt(0);
            int row_num = 0;
            // Iterate through rows
            for (Row row : sheet) {
                if (row_num == 0) {
                    createHeaderMap(row, headerMap);
                    row_num++;
                    continue;
                }
                createValueMap(row, valueMap, headerMap);
            }
        }
            catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static void createValueMap(Row row, Map<String, Double> valueMap, Map<Integer, String> headerMap) {

        Iterator<Cell> cellIterator = row.cellIterator();
        String age = null;
        String key = null;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if(cell.getColumnIndex() == 0){
                switch (cell.getCellType()) {
                    case STRING:{
                        age = cell.getStringCellValue();
                        break;
                    }

                    case NUMERIC:{
                        age = String.valueOf((long)cell.getNumericCellValue());
                        break;
                    }
                }
                continue;
            }


            key = age + "#" +headerMap.get(cell.getColumnIndex());
            valueMap.put(key, cell.getNumericCellValue());
        }
    }

    private static void createHeaderMap(Row row, Map<Integer, String> headerMap) {
            headerMap.put(1, "500000");
            headerMap.put(2, "750000");
            headerMap.put(3, "1000000");
            headerMap.put(4, "1500000");
            headerMap.put(5, "2000000");
            headerMap.put(6, "2500000");
            headerMap.put(7, "5000000");
            headerMap.put(8, "10000000");
            headerMap.put(9, "UNLIMITED");
    }
}

