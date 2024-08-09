package com.quotes.premium.reader;

import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

public class HospitalCash {

    static final Map<String, Double> individualMap = new TreeMap<>();
    static final Map<String, Double> floaterMap = new TreeMap<>();

    public static Double get(final int age, final String policyType, final String hospitalDay) {
        if("individual".equals(policyType)){
            HospitalCash.map("hospital_cash_individual.xlsx", HospitalCash.individualMap);
            final String ageBand = HospitalCash.findAgeBand(age, HospitalCash.individualMap);
            return HospitalCash.individualMap.get(ageBand+"#"+hospitalDay);
        }

        else{
            HospitalCash.map("hospital_cash_floater.xlsx", HospitalCash.floaterMap);
            final String ageBand = HospitalCash.findAgeBand(age, HospitalCash.floaterMap);
            return HospitalCash.floaterMap.get(ageBand);
        }
    }

    private static String findAgeBand(final int age, final Map<String, Double> map) {
        for (final Map.Entry<String, Double> entry : FutureReadyConf.map.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith("GT")) {
                // Handle "GT" case
                if (age > Integer.parseInt(key.substring(3).trim())) {
                    return key;
                }
            } else if (key.startsWith("LTE")) {
                // Handle "LTE" case
                if (age <= Integer.parseInt(key.substring(4).trim())) {
                    return key;
                }
            } else {
                // Handle range case
                final String[] parts = key.split("-");
                final int start = Integer.parseInt(parts[0]);
                final int end = Integer.parseInt(parts[1]);
                if (age >= start && age <= end) {
                    return key;
                }
            }
        }
        return null ;
    }

    private static void floaterMap() {
    }

    private static void map(final String fileName, final Map<String,Double> map) {
        try (final InputStream file = FutureReadyConf.class.getClassLoader().getResourceAsStream(fileName)) {
            assert null != file;
            try (final Workbook workbook = WorkbookFactory.create(file)) {
                final Sheet sheet = workbook.getSheetAt(0);
                final Row hospitalDays = sheet.getRow(0);
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    final Row row = sheet.getRow(rowIndex);
                    if (null == row) continue;

                    for (int cellIndex = 1; cellIndex < row.getLastCellNum(); cellIndex++) {
                        final Cell cell = row.getCell(cellIndex);
                        if (null == cell) continue;

                        final String ageBand = row.getCell(0).getStringCellValue().trim();
                        final String day = String.valueOf((int)hospitalDays.getCell(cellIndex).getNumericCellValue());
                        final String key = ageBand + "#" + day;
                        map.put(key, cell.getNumericCellValue());
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) {
        HospitalCash.map("hospital_cash_individual.xlsx", HospitalCash.individualMap);
        HospitalCash.map("hospital_cash_floater.xlsx", HospitalCash.floaterMap);
        System.out.println(HospitalCash.individualMap.keySet());
        System.out.println(HospitalCash.individualMap.values());

        System.out.println(HospitalCash.floaterMap.keySet());
        System.out.println(HospitalCash.floaterMap.values());
    }
}
