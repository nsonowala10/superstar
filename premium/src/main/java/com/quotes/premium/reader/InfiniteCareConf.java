package com.quotes.premium.reader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InfiniteCareConf {

    @Value("${infinite.care.mapping}")
    private String infiniteCareMapping;

    private final Map<String, String> infiniteCareMap = new HashMap<>();

    public String infiniteCare(final String amount){
        if(this.infiniteCareMap.isEmpty()){
            final String[] item = this.infiniteCareMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.infiniteCareMap.put(each[0], each[1]);
            }
        }
        return this.infiniteCareMap.get(amount);
    }
}
