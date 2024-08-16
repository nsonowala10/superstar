package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.TreeMap;

@Configuration
public class WomenCare {
    @Value("${women.care.expense.mapping}")
    private String womenCareMapping;

    public final TreeMap<Double, Double> womenCare = new TreeMap<>();

    public Double getWomenCareExpense(final Double sumInsured){
        if(this.womenCare.isEmpty()){
            final String[] item = this.womenCareMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.womenCare.put(Double.valueOf(each[0]), Double.valueOf(each[1]));
            }
        }
        Map.Entry<Double, Double> entry = this.womenCare.floorEntry(sumInsured);
        return (entry != null) ? entry.getValue() : 0.0d;
    }
}
