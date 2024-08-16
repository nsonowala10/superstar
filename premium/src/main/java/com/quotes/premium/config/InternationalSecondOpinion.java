package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InternationalSecondOpinion {
    @Value("${international.second.opinion.mapping}")
    private String internationalSecondOpinionMapping;

    private final Map<String, Double> internalSecondOpinion = new HashMap<>();
    public Double getInternationSecondOpinion(final String roomType){
        if(this.internalSecondOpinion.isEmpty()){
            final String[] item = this.internationalSecondOpinionMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.internalSecondOpinion.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.internalSecondOpinion.get(roomType);
    }
}
