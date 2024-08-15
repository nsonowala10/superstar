package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ZonalDiscount {
    @Value("${zonal.discount.mapping}")
    private String zonalDiscountMapping;

    private final Map<String, Double> zonalDiscount = new HashMap<>();
    public Double getZonalDiscount(final String zone){
        if(this.zonalDiscount.isEmpty()){
            final String[] item = this.zonalDiscountMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.zonalDiscount.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.zonalDiscount.get(zone);
    }
}
