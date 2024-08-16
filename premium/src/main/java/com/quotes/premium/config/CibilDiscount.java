package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.TreeMap;

@Configuration
public class CibilDiscount {

    @Value("${cibil.discount.mapping}")
    private String cibilDiscounts;

    public final TreeMap<Integer, Double> cibilDiscountMap = new TreeMap<>();

    public Double cibilDiscount(final Integer cibil){
        if(this.cibilDiscountMap.isEmpty()){
            final String[] item = this.cibilDiscounts.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.cibilDiscountMap.put(Integer.valueOf(each[0]), Double.valueOf(each[1]));
            }
        }
        Map.Entry<Integer, Double> entry = this.cibilDiscountMap.floorEntry(cibil);
        return (entry != null) ? entry.getValue() : 0.0d;
    }
}
