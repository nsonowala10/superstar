package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Applicables {

    private Double basePremium;
    private Double zoneDiscount = 0.0d;
    private Double policyTermDiscount = 0.0d;
    private Double policyTypeDiscount = 0.0d;
    private Double preferredHospitalDiscount = 0.0d;
    private Double copayDiscount = 0.0d;
    private Double deductibleDiscount = 0.0d;
    private Double powerBoosterLoading = 0.0d;
    private Double instantCoverLoading = 0.0d;
    private Double consumableCoverLoading = 0.0d;
    private Double futureReadyLoading = 0.0d;
    private Double pedWaitingPeriodLoading = 0.0d;
    private Double specificDiseaseLoading = 0.0d;
    private Double infiniteCareLoading = 0.0d;
    private Double sharedRoomDiscount = 0.0d;
    private Double subLimitForModernTreatmentsDiscount = 0.0d;
    private Double durableMedicalEquipmentCoverLoading = 0.0d;
    private Double wellnessDiscount = 0.0d;
    private Double nriDiscount = 0.0d;
    private Double roomRentDiscount = 0.0d;
    private Double maternityExpense = 0.0d;
    private Double womenCareExpense = 0.0d;
    private Double highEndDiagnosticExpense = 0.0d;
    private Double annualHealthCheckUpExpense = 0.0d;
    private Double internationalSecondOpinionExpense = 0.0d;
    private Double compassionateVisitExpense = 0.0d;
    private Double hospitalCashExpense = 0.0d;
    private Double paCoverExpense = 0.0d;
    private Double healthQuestionnaireDiscount = 0.0d;
    private Double cibilDiscount = 0.0d;
    private Double earlyRenewalDiscount;
    private Double longTermDiscount;
    private Double reflexLoadingExpense;

    private Double stageIVSum = 0.0d;
    private Double stageIIISum = 0.0d;
    private Double stageIISum = 0.0d;
    private Double stageISum = 0.0d;


    private String type;
    private int age;
    private int year;
    private List<String> peds = new ArrayList<>();
    private boolean nri;
    private boolean proposer;
    private Double reflexLoadingPercentage;



    public void prepareStageIISum(){
        this.stageIISum = this.basePremium
                + this.powerBoosterLoading
                + this.instantCoverLoading
                + this.consumableCoverLoading
                + this.futureReadyLoading
                + this.specificDiseaseLoading
                + this.pedWaitingPeriodLoading
                + this.infiniteCareLoading
                - this.preferredHospitalDiscount
                - this.copayDiscount
                - this.deductibleDiscount
                - this.roomRentDiscount
                - this.subLimitForModernTreatmentsDiscount
                + this.durableMedicalEquipmentCoverLoading
                - this.wellnessDiscount
                - this.nriDiscount
                + this.maternityExpense
                + this.womenCareExpense
                + this.highEndDiagnosticExpense
                + this.annualHealthCheckUpExpense
                + this.internationalSecondOpinionExpense
                + this.compassionateVisitExpense
                + this.hospitalCashExpense
                + this.paCoverExpense;
    }

    public void prepareStageISum(){
        this.stageISum = this.basePremium = this.basePremium
                - this.zoneDiscount
                - this.policyTypeDiscount
                + this.reflexLoadingExpense;
    }

    public void prepareStageIIISum() {
        this.stageIIISum = this.stageIISum
                - this.healthQuestionnaireDiscount
                - this.cibilDiscount
                - this.earlyRenewalDiscount;
    }

    public void prepareStageIVSum() {
        this.stageIVSum = this.stageIIISum
                - this.longTermDiscount;
    }
}
