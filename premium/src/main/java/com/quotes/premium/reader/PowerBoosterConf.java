package com.quotes.premium.reader;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

public class PowerBoosterConf {

    @Value("${power.booster.mapping}")
    private String powerBoosters;

    private final Map<String, String> powerBoostersMap = new HashMap<>();

    public String powerBooster(final String amount){
        if(this.powerBoostersMap.isEmpty()){
            final String[] item = this.powerBoosters.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.powerBoostersMap.put(each[0], each[1]);
            }
        }
        return this.powerBoostersMap.get(amount);
    }
}
