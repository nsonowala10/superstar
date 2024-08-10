package com.quotes.premium.reader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PowerBoosterConf {

    @Value("${power.booster.mapping}")
    private String powerBoosters;

    private final Map<String, Double> powerBoostersMap = new HashMap<>();

    public Double powerBooster(final String amount){
        if(this.powerBoostersMap.isEmpty()){
            final String[] item = this.powerBoosters.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.powerBoostersMap.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.powerBoostersMap.get(amount);
    }
}
