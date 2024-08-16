package com.quotes.premium.config;

import com.quotes.premium.reader.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class DynamicConfigurations {

    @Autowired
    private InfiniteCareConf infiniteCareConf;
    @Autowired
    private PowerBoosterConf powerBoosterConf;
    @Autowired
    private CibilDiscount cibilDiscount;
    @Autowired
    private ZonalDiscount zonalDiscount;
    @Autowired
    private RoomRent roomRent;
    @Autowired
    private WellnessDiscount wellnessDiscount;
    @Autowired
    private WomenCare womenCare;
    @Autowired
    private LongTermDiscount longTermDiscount;
    @Autowired
    private PolicyTypeDiscount policyTypeDiscount;
    @Autowired
    private CompassionateVisit compassionateVisit;
    @Autowired
    private InternationalSecondOpinion internationalSecondOpinion;
    @Autowired
    private HighEndDiagnostic highEndDiagnostic;

    public static final Map<String, Double> voluntaryDeductible = new HashMap<>();
    public static final Map<String, Double> reductionOfPEWaitingPeriod = new HashMap<>();
    public static final Map<String, Double> annualHealthCheck = new HashMap<>();

    public Double getInfiniteCare(final String amount){
        return this.infiniteCareConf.infiniteCare(amount);
    }

    public Double getCibilDiscount(final Integer cibil) {
        return this.cibilDiscount.cibilDiscount(cibil);
    }

    public Double getZonalDiscount(final String zone) {return this.zonalDiscount.getZonalDiscount(zone);}

    public Double getRoomRentDiscount(final String roomType) {return this.roomRent.getRoomRentDiscount(roomType);}

    public Double getWellnessDiscount(final Double wellnessPoint) {return this.wellnessDiscount.getWellnessDiscount(wellnessPoint);}

    public Double getWomenCareExpense(final Double sumInsured) {return this.womenCare.getWomenCareExpense(sumInsured);}

    public Double getLongTermDiscount(final Integer year) {return this.longTermDiscount.getLongTermDiscount(year);}

    public Double getPolicyTypeDiscount(final String policyType) {return this.policyTypeDiscount.getPolicyTypeDiscount(policyType);}

    public Double getCompassionateVisit(final String policyType) {return this.compassionateVisit.getCompassionateVist(policyType);}

    public Double getInternationalSecondOpinion(final String policyType) {return this.internationalSecondOpinion.getInternationSecondOpinion(policyType);}
    public Double getHighEndDiagnostic(final String policyType) {return this.highEndDiagnostic.getHighEndDiagnostic(policyType);}
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
