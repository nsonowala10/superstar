package com.quotes.premium.config;

import com.quotes.premium.reader.*;
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

    public static final Map<String, Double> voluntaryDeductible = new HashMap<>();
    public static final Map<String, Double> reductionOfPEWaitingPeriod = new HashMap<>();
    public static final Map<String, Double> annualHealthCheck = new HashMap<>();

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

    public Double getInfiniteCare(final String amount){
        return this.infiniteCareConf.infiniteCare(amount);
    }

    public double getPowerBooster(final String amount){
        return this.powerBoosterConf.powerBooster(amount);
    }

    public static Map<String,Double> getPowerBoosterConfig(){
        return DynamicConfigurations.powerBoosterConfig;
    }

    public static List<String> getCopayAllowedValues(){
        return DynamicConfigurations.copayAllowedValues;
    }

    public static Double getFutureReadyconf(int age){
        return FutureReadyConf.get(age);
    }

    public static Double getSpecificDiseaseConf(int age){
        return SpecificDiseaseConf.get(age);
    }


    public static Double getVoluntaryDeductiblePercent(final int age, final int deductible) {
        if(voluntaryDeductible.isEmpty()) {
            VoluntaryDeductible.fetchVoluntaryDeductibles("VoluntaryDeductible.xlsx");
        }
        return VoluntaryDeductible.getVoluntaryDeductiblePercent(age, deductible);
    }
    public static Double getReductionOfPEDWaitingPercent(final int age, final String waitingPeriodInMonths) {
        if(reductionOfPEWaitingPeriod.isEmpty()) {
            PedWaitingConfig.fetchReductionOfPEDWaiting("ReductionOnPED.xlsx");
        }
        return PedWaitingConfig.getPedValue(age, waitingPeriodInMonths);
    }
    public static Double getAnnualCheckUp(final String policyType, final String sumInsured) {
        if(annualHealthCheck.isEmpty()) {
            AnnualCheckUpConfig.fetchAnnualCheckUp("AnnualCheckUp.xlsx");
        }
        return AnnualCheckUpConfig.getAnnualCheckUp(policyType,sumInsured);
    }

    public static Double getHospitalCash(final String policyType, final int age, final String hospitalDays) {
        return HospitalCash.get(age, policyType, hospitalDays);
    }



}
