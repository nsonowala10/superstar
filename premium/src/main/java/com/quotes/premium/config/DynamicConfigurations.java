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

    public Double getInfiniteCare(final String amount){
        return this.infiniteCareConf.infiniteCare(amount);
    }

    public double getPowerBooster(final String amount){
        return this.powerBoosterConf.powerBooster(amount);
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
