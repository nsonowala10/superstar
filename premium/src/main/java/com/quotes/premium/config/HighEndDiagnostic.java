package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HighEndDiagnostic {
    @Value("${high.end.diagnostic.mapping}")
    private String highEndDiagnosticMapping;

    private final Map<String, Double> highEndDiagnostic = new HashMap<>();
    public Double getHighEndDiagnostic(final String roomType){
        if(this.highEndDiagnostic.isEmpty()){
            final String[] item = this.highEndDiagnosticMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.highEndDiagnostic.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.highEndDiagnostic.get(roomType);
    }
}
