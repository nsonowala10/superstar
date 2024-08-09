package com.quotes.premium.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicConfigurations {

    private static final Map<String, Double> powerBoosterConfig = Map.of(
            "500000", 15.0d,
            "750000", 14.0d,
            "1000000", 13.0d,
            "1500000", 12.0d,
            "2000000", 11.0d,
            "2500000", 10.0d,
            "5000000", 9.0d,
            "10000000", 8.0d,
            "UNLIMITED", 0.0d
    );

    private static final List<String> copayAllowedValues = Arrays.asList("10", "20", "30", "40", "50");
    private static final Map<String, Double> infiniteCareConfig = Map.of(
            "500000", 30d,
            "750000", 28d,
            "1000000", 26d,
            "1500000", 24d,
            "2000000", 22d,
            "2500000", 20d,
            "5000000", 18d,
            "10000000", 16d
    );

    private static final Map<String, Double> futureReadyConf = new HashMap<>();

    public static Map<String,Double> getPowerBoosterConfig(){
        return DynamicConfigurations.powerBoosterConfig;
    }

    public static List<String> getCopayAllowedValues(){
        return DynamicConfigurations.copayAllowedValues;
    }

    public static Map<String,Double> getInfiniteCareConfig(){
        return DynamicConfigurations.infiniteCareConfig;
    }

    public static Double getFutureReadyConf(final String age){
        if(DynamicConfigurations.futureReadyConf.isEmpty()){

        }
        return DynamicConfigurations.futureReadyConf.get(age);
    }
}
