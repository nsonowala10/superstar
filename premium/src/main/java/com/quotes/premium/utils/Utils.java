package com.quotes.premium.utils;

import com.quotes.premium.dto.Applicables;
import com.quotes.premium.dto.Attribute;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static List<Applicables> get(final Attribute attribute, final List<Applicables> list){
        List<Applicables> filteredList = handleYear(attribute, list);
        return handleInsured(filteredList, attribute);
    }

    private static List<Applicables> handleInsured(List<Applicables> applicableList, Attribute attribute) {
        if("oldest".equals(attribute.getInsured())){
            Optional<Integer> maxAge = applicableList.stream()
                    .map(Applicables::getAge)
                    .max(Integer::compareTo);

            return applicableList.stream().filter(app -> app.getAge() == maxAge.get()).toList();
        }

        else if("adult".equals(attribute.getInsured())){
           return applicableList.stream().filter(app -> "adult".equals(app.getType())).toList();
        }

        else if("child".equals(attribute.getInsured())){
            return applicableList.stream().filter(app -> "child".equals(app.getType())).toList();
        }

        return applicableList;
    }

    private static List<Applicables> handleYear(Attribute attribute, List<Applicables> applicables) {
        if(attribute.getYear().equals("inception")){
            return applicables.stream().filter(app->app.getYear() == 1).toList();
        }

        return applicables;
    }

    private static List<Map<String, Applicables>> handleAllYear(Map<Integer, Map<String, Applicables>> map) {
        List<Map<String, Applicables>> ls  = new ArrayList<>();
        for(Map.Entry<Integer, Map<String, Applicables>> entry : map.entrySet()){
            ls.add(entry.getValue());
        }
        return ls;
    }

    private static List<Map<String, Applicables>> handleInceptionYear(Map<Integer, Map<String, Applicables>> map) {
        List<Map<String, Applicables>> ls  = new ArrayList<>();
        ls.add(map.get(1));
        return ls;
    }
}
