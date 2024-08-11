package com.quotes.premium.utils;

import com.quotes.premium.dto.Applicable;
import com.quotes.premium.dto.Attribute;

import java.util.*;

public class Utils {

    public static List<Applicable> get(final Attribute attribute, final List<Applicable> list){
        final List<Applicable> filteredList = Utils.handleYear(attribute, list);
        return Utils.handleInsured(filteredList, attribute);
    }

    private static List<Applicable> handleInsured(final List<Applicable> applicableList, final Attribute attribute) {
        final String insuredType = attribute.getInsured();
        return switch (insuredType) {
            case "oldest" -> {
                final Optional<Integer> maxAge = applicableList.stream()
                        .map(Applicable::getAge)
                        .max(Integer::compareTo);
                yield applicableList.stream()
                        .filter(app -> app.getAge() == (maxAge.orElse(0)))
                        .toList();
            }
            case "proposer", "adult", "child" -> applicableList.stream()
                    .filter(app -> insuredType.equals(app.getType()))
                    .toList();
            default -> applicableList;
        };
    }


    private static List<Applicable> handleYear(final Attribute attribute, final List<Applicable> applicables) {
        return "inception".equals(attribute.getYear())
                ? applicables.stream().filter(app -> 1 == app.getYear()).toList()
                : applicables;
    }
}
