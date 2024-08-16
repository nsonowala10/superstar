package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LongTermDiscount {
    @Value("${long.term.discount.mapping}")
    private String longTermDiscountMapping;

    private final Map<Integer, Double> longTermDiscount = new HashMap<>();
    public Double getLongTermDiscount(final Integer year){
        if(this.longTermDiscount.isEmpty()){
            final String[] item = this.longTermDiscountMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.longTermDiscount.put(Integer.valueOf(each[0]), Double.valueOf(each[1]));
            }
        }
        return this.longTermDiscount.get(year);
    }
}
