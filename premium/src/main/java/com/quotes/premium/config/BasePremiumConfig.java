package com.quotes.premium.config;

import com.quotes.premium.dto.Insured;
import com.quotes.premium.reader.ExcelReader;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BasePremiumConfig {

    private Map<String, Double> parentPremium = new HashMap<>();
    private Map<String, Double> childPremium = new HashMap<>();

    @Autowired
    private ExcelReader excelReader;

    @PostConstruct
    public void config(){
        this.excelReader.preparePremium(parentPremium, "parent_ss_premium.xlsx" );
        this.excelReader.preparePremium(childPremium, "child_ss_premium.xlsx" );
        System.out.println(parentPremium.get("40#500000"));
        System.out.println(childPremium.get("16-20#500000"));
    }


    public Double getPremium(int age, String type, String sumInsured){
        String key = null;
        sumInsured = Long.valueOf(sumInsured) > 10000000 ? "UNLIMITED" : sumInsured;
        switch(type){
            case "parent":{
                key = age > 80 ? ">80" : age + "#" +sumInsured;
                return this.parentPremium.get(key);
            }
            case "child":{
                String ageString = null;
                if(age <= 15){
                    ageString = "<=15";
                }

                else if( Long.valueOf(age) <= 20){
                    ageString = "16-20";
                }

                else if( Long.valueOf(age) <= 25){
                    ageString = "21-25";
                }

                key = ageString + "#" +sumInsured;
                return this.childPremium.get(key);
            }
        }

        return null;
    }
}
