package com.quotes.premium.reader;

import org.apache.poi.ss.usermodel.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FutureReadyConf {
    static final Map<String, Double> map = new TreeMap<>();

    public static void futureReadyConf() {
        if(!FutureReadyConf.map.isEmpty())
            return ;

        final String filename = "future_ready_conf.xlsx";
        try (final InputStream file = FutureReadyConf.class.getClassLoader().getResourceAsStream(filename)) {
            assert null != file;
            try (final Workbook workbook = WorkbookFactory.create(file)) {
                 final Sheet sheet = workbook.getSheetAt(0);
                for (final Row row : sheet) {
                    final Iterator<Cell> cellIterator = row.cellIterator();
                    if (cellIterator.hasNext()) {
                        final Cell keyCell = cellIterator.next(); // First cell
                        if (cellIterator.hasNext()) {
                            final Cell valueCell = cellIterator.next(); // Second cell
                            FutureReadyConf.map.put(keyCell.toString(), valueCell.getNumericCellValue());
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Double get(final int age) {
        FutureReadyConf.futureReadyConf();
        final String ageBand = FutureReadyConf.findAgeRange(age);
        return FutureReadyConf.map.get(ageBand);
    }

    public static String findAgeRange(final int age) {
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

    public static void main(final String[] args) {
        FutureReadyConf.futureReadyConf();
        System.out.println(FutureReadyConf.map.values());
    }
}
