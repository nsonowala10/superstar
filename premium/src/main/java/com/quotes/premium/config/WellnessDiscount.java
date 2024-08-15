package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.TreeMap;

@Configuration
public class WellnessDiscount {
    @Value("${wellness.discount.mapping}")
    private String wellnessDiscountMapping;

    public final TreeMap<Double, Double> wellnessDiscount = new TreeMap<>();

    public Double getWellnessDiscount(final Double wellnessPoint){
        if(this.wellnessDiscount.isEmpty()){
            final String[] item = this.wellnessDiscountMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.wellnessDiscount.put(Double.valueOf(each[0]), Double.valueOf(each[1]));
            }
        }
        Map.Entry<Double, Double> entry = this.wellnessDiscount.floorEntry(wellnessPoint);
        return (entry != null) ? entry.getValue() : 0.0d;
    }
}
