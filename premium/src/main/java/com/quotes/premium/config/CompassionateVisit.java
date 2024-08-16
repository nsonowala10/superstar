package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CompassionateVisit {
    @Value("${compassionate.visit.mapping}")
    private String compassionateVisitMapping;

    private final Map<String, Double> compassionateVisit = new HashMap<>();
    public Double getCompassionateVist(final String roomType){
        if(this.compassionateVisit.isEmpty()){
            final String[] item = this.compassionateVisitMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.compassionateVisit.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.compassionateVisit.get(roomType);
    }
}
