package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class PolicyTypeDiscount {
    @Value("${policy.type.discount.mapping}")
    private String policyTypeDiscountMapping;

    private final Map<String, Double> policyTypeDiscount = new HashMap<>();
    public Double getPolicyTypeDiscount(final String roomType){
        if(this.policyTypeDiscount.isEmpty()){
            final String[] item = this.policyTypeDiscountMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.policyTypeDiscount.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.policyTypeDiscount.get(roomType);
    }
}
