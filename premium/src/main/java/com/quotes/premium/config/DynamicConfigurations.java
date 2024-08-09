package com.quotes.premium.config;

import com.quotes.premium.reader.FutureReadyConf;
import com.quotes.premium.reader.InfiniteCareConf;
import com.quotes.premium.reader.PowerBoosterConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DynamicConfigurations {

    @Autowired
    private InfiniteCareConf infiniteCareConf;
    @Autowired
    private PowerBoosterConf powerBoosterConf;

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

    public String getInfiniteCare(final String amount){
        return this.infiniteCareConf.infiniteCare(amount);
    }

    public String getPowerBooster(final String amount){
        return this.powerBoosterConf.powerBooster(amount);
    }

    public static Map<String,Double> getPowerBoosterConfig(){
        return DynamicConfigurations.powerBoosterConfig;
    }

    public static List<String> getCopayAllowedValues(){
        return DynamicConfigurations.copayAllowedValues;
    }
}
