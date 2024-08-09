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
        this.excelReader.preparePremium(parentPremium, "/Users/anil.juneja/Downloads/parent_ss_premium.xlsx" );
        this.excelReader.preparePremium(childPremium, "/Users/anil.juneja/Downloads/child_ss_premium.xlsx" );
        System.out.println(parentPremium.get("40#500000"));
        System.out.println(childPremium.get("16-20#500000"));
    }

    public Double getPremium(Insured insured, String sumInsured){
        String key = null;
        String age = null;
        sumInsured = Long.valueOf(sumInsured) > 10000000 ? "UNLIMITED" : sumInsured;
        switch(insured.getType()){
            case "parent":{
                age = Long.valueOf(insured.getAge()) > 80 ? ">80" : String.valueOf(insured.getAge());
                key = age + "#" +sumInsured;
                return this.parentPremium.get(key);
            }
            case "child":{
                if(insured.getAge() <= 15){
                    age = "<=15";
                }

                else if(insured.getAge() <= 20){
                    age = "16-20";
                }

                else if(insured.getAge() <= 25){
                    age = "21-25";
                }

                key = age + "#" +sumInsured;
                return this.childPremium.get(key);
            }
        }

        return null;
    }
}
