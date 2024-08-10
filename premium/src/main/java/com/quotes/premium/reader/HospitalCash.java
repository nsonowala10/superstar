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
            return HospitalCash.floaterMap.get(ageBand + "#" +hospitalDay);
        }
    }

    private static String findAgeBand(final int age, final Map<String, Double> map) {
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

        return ageBand;
    }

    private static void floaterMap() {
    }

    private static void map(final String fileName, final Map<String,Double> map) {
        if(!map.isEmpty()){
            return ;
        }

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
