package com.quotes.premium.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quotes.premium.config.BasePremiumConfig;
import com.quotes.premium.config.DynamicConfigurations;
import com.quotes.premium.config.MandatoryConfiguration;
import com.quotes.premium.dto.*;
import com.quotes.premium.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PremiumService {

    @Autowired
    private BasePremiumConfig premiumConfig;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private DynamicConfigurations dynamicConfigurations;
    @Autowired
    private MandatoryConfiguration mandatoryConfiguration;

    public AmountDivision calculatePremium(final PremiumRequest premiumRequest) throws Exception {
        this.validationService.validatePremiumRequest(premiumRequest);
        final AmountDivision amountDivision = new AmountDivision();
        this.createInsuredMapping(amountDivision, premiumRequest);
        this.stage1(amountDivision, premiumRequest);
        this.stage2(amountDivision, premiumRequest);
        this.stage3(amountDivision, premiumRequest);
        this.stage4(amountDivision);
        this.stage5(amountDivision);
        return amountDivision;
    }

    private void createInsuredMapping(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        for(int year =1;year <= premiumRequest.getPolicyTerm();year ++){
            for(final Insured insured : premiumRequest.getInsured()){
                final Applicable applicable = new Applicable();
                applicable.setAge(insured.getAge());
                applicable.setType(insured.getType());
                applicable.setYear(year);
                applicable.setPeds(insured.getPeds());
                applicable.setNri(insured.isNri());
                applicable.setProposer(insured.isProposer());
                applicable.setReflexLoadingPercentage(insured.getReflexLoading()); // TODO remove it
                amountDivision.getApplicables().add(applicable);
            }
        }
    }

    private void stage5(final AmountDivision amountDivision) {
        double finalPremium = 0.0;
        for(final Applicable app : amountDivision.getApplicables()){
            finalPremium = finalPremium + app.getStageIVSum();
        }
        amountDivision.setFinalPremium(finalPremium);
    }

    private void stage4(final AmountDivision amountDivision) {
        this.longTermDiscount(amountDivision);
        amountDivision.getApplicables().forEach(Applicable::prepareStageIVSum);
    }

    private void longTermDiscount(final AmountDivision amountDivision) {
        final Map<Integer, Double> termMap = Map.of(1,0.0d,2,0.10d,3,0.125d,4,0.150d,5,0.150d);
        amountDivision.getApplicables().forEach(app -> {
            app.setLongTermDiscount(app.getLongTermDiscount() + app.getStageIIISum()*termMap.get(app.getYear()));
        });
    }

    private void stage3(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        this.healthQuestionnaire(amountDivision, premiumRequest);
        this.cibilDiscount(amountDivision, premiumRequest);
        this.earlyRenewalDiscount(amountDivision, premiumRequest);
        amountDivision.getApplicables().forEach(Applicable::prepareStageIIISum);

        /* TODO capping */
    }

    private void earlyRenewalDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isEarlyRenewalDiscount()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("earlyRenewal",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> app.setEarlyRenewalDiscount(app.getEarlyRenewalDiscount() + app.getStageIISum()*0.025d));
    }

    private void cibilDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null ==  premiumRequest.getCibilScoreRequest() || !premiumRequest.getCibilScoreRequest().isCibil()){
            return ;
        }

        final int cibil = premiumRequest.getCibilScoreRequest().getCibilScore();
        final Double discount;
        if(801 <= cibil){
            discount = 0.075d;
        }

        else if(751 <= cibil){
            discount = 0.05d;
        }

        else if(701 <= cibil){
            discount = 0.025d;
        } else {
            discount = 0.0d;
        }
        final List<Applicable> ls = this.determineConfiguration("cibilDiscount",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> app.setCibilDiscount(app.getCibilDiscount() + app.getStageIISum()*discount));
    }

    private void healthQuestionnaire(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isHealthQuestionnaire()){
            return ;
        }
        final List<Applicable> ls = this.determineConfiguration("healthQuestionnaire",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> app.setHealthQuestionnaireDiscount(app.getHealthQuestionnaireDiscount() + app.getStageIISum()*0.1d));
    }

    private void stage2(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        this.applyPowerBooster(amountDivision, premiumRequest);
        this.instantCover(amountDivision, premiumRequest);
        this.consumableCover(amountDivision,premiumRequest);
        this.futureReady(amountDivision,premiumRequest);
        this.specificDisease(amountDivision, premiumRequest);
        this.pedWaitingPeriod(amountDivision, premiumRequest);
        this.infiniteCare(amountDivision, premiumRequest);
        this.preferredHospitalNetwork(amountDivision, premiumRequest);
        this.voluntarilyCopay(amountDivision, premiumRequest);
        this.voluntarilyDeductible(amountDivision, premiumRequest);
        this.roomRentModification(amountDivision, premiumRequest);
        this.subLimitModeration(amountDivision, premiumRequest);
        this.medicalEquipmentCover(amountDivision, premiumRequest);
        this.wellnessDiscount(amountDivision, premiumRequest);
        this.nriDiscount(amountDivision, premiumRequest);
        this.maternity(amountDivision, premiumRequest);
        this.womenCare(amountDivision, premiumRequest);
        this.highEndDiagnostic(amountDivision, premiumRequest);
        this.annualHealthCheckUp(amountDivision, premiumRequest);
        this.internationalSecondOpinion(amountDivision, premiumRequest);
        this.compassionateVisit(amountDivision, premiumRequest);
        this.hospitalCash(amountDivision, premiumRequest);
        this.paCover(amountDivision, premiumRequest);
        amountDivision.getApplicables().forEach(Applicable::prepareStageIISum);
        return ;
    }

    private void paCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getPaCoverRequest() || !premiumRequest.getPaCoverRequest().isPaCover()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("paCover",amountDivision, premiumRequest.getPolicyType());
        final String option = premiumRequest.getPaCoverRequest().getOption();
        final Double perMile = "1".equals(option) ? 1.0d : 2.0d;
        final Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        final Double perMileExpense = sumInsured * perMile / 1000.0d;
        final int maxAge = PremiumService.getMaxAge(ls).orElse(0);
        ls.forEach(app -> {
            if(app.getAge() == maxAge){
                app.setPaCoverExpense(app.getPaCoverExpense() + perMileExpense);
            }
            else{
                app.setPaCoverExpense(app.getPaCoverExpense() + perMileExpense/2.0d);
            }
        });
    }

    private void hospitalCash(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getHospitalCashRequest() || !premiumRequest.getHospitalCashRequest().isHospitalCash()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("hospitalCash",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> {
            final Double expense = DynamicConfigurations.getHospitalCash(premiumRequest.getPolicyType(), app.getAge(), premiumRequest.getHospitalCashRequest().getNumberOfDays());
            app.setHospitalCashExpense(app.getHospitalCashExpense() + expense);
        });
    }

    private void compassionateVisit(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isCompassionateVisit()){
            return ;
        }

        final List<Applicable> ls =  this.determineConfiguration("CompassionateVisit",amountDivision, premiumRequest.getPolicyType());
        final Double expense = "floater".equals(premiumRequest.getPolicyType())? 100.0d : 50.0d;
        ls.forEach(app -> app.setCompassionateVisitExpense(app.getCompassionateVisitExpense() + expense));
    }

    private void internationalSecondOpinion(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isInternationalSecondOpinion()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("internationalSecondOpinion",amountDivision, premiumRequest.getPolicyType());
        final Double expense = "floater".equals(premiumRequest.getPolicyType())? 20.0d : 15.0d;
        ls.forEach(app -> app.setInternationalSecondOpinionExpense(app.getInternationalSecondOpinionExpense() + expense));
    }

    private void annualHealthCheckUp(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isAnnualCheckUp()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("annualHealthCheckUp",amountDivision, premiumRequest.getPolicyType());
        final Double expense = DynamicConfigurations.getAnnualCheckUp(premiumRequest.getPolicyType(),premiumRequest.getSumInsured());
        ls.forEach(app -> app.setAnnualHealthCheckUpExpense(app.getAnnualHealthCheckUpExpense() + expense));
    }

    private void highEndDiagnostic(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isHighEndDiagnostic()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("highEndDiagnostic",amountDivision, premiumRequest.getPolicyType());
        final Double amount = "floater".equals(premiumRequest.getPolicyType()) ? 1000.0d : 750.0d ;
        ls.forEach(app -> app.setHighEndDiagnosticExpense(app.getHighEndDiagnosticExpense() + amount));
    }

    private void womenCare(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isWomenCare()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("womenCare",amountDivision, premiumRequest.getPolicyType());
        final double sumInsured = Double.parseDouble(premiumRequest.getSumInsured());
        double expense = 0.0d;
        if(2500000.0d >= sumInsured){
            expense = 500.0d;
        }
        else{
            expense = 500.0d;
        }
        final Double finalExpense = expense;
        ls.forEach(app -> app.setWomenCareExpense(app.getWomenCareExpense() + finalExpense));
    }

    private void maternity(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getMaternityRequest() || !premiumRequest.getMaternityRequest().isMaternityRequest()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("maternityExpense",amountDivision, premiumRequest.getPolicyType());
        final List<MaternityOptions> maternityOptions = premiumRequest.getMaternityRequest().getOption();
        final Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        ls.forEach(app -> {
            PremiumService.applyMaterityExpense(maternityOptions, sumInsured, app);
        });

    }

    private static void applyMaterityExpense(final List<MaternityOptions> maternityOptions, final Double sumInsured, final Applicable applicable) {
        for(final MaternityOptions option : maternityOptions){
            Double maternityAmount = 0.0d;
            Double newBornAmount = 0.0d;
            switch (option.getOption()){
                case "A":{
                    if(1500.0d == option.getSubLimit()){
                        maternityAmount = 15000.0d;
                    }

                    else if(10000.0d == option.getSubLimit()){
                        maternityAmount = 15000.0d;
                    }

                    if(2500000 >= sumInsured){
                        newBornAmount = 1000.0d;
                    }

                    else {
                        newBornAmount = 2000.0d;
                    }

                    break;
                }
                case "B":{
                    if(30000.0d == option.getSubLimit()){
                        maternityAmount = 500.0d;
                    }

                    if(2500000 >= sumInsured){
                        newBornAmount = 2000.0d;
                    }

                    else {
                        newBornAmount = 2500.0d;
                    }

                    break;

                }
                case "C":{
                    if(500000 == sumInsured || 750000 == sumInsured){
                        newBornAmount = 10000.0d;
                    }

                    else if(1000000 == sumInsured || 1500000 == sumInsured || 2000000 == sumInsured || 2500000 == sumInsured){
                        newBornAmount = 10000.0d;
                    }

                    else{
                        newBornAmount = 10000.0d;
                    }
                    break;
                }
            }

            applicable.setMaternityExpense(applicable.getMaternityExpense() + maternityAmount + newBornAmount);
        }
    }

    private void nriDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        final List<Applicable> ls = this.determineConfiguration("wellnessDiscount",amountDivision, premiumRequest.getPolicyType());
        for(final Applicable app : ls){
            if(!app.isNri()){
                return ;
            }
        }

        ls.forEach(app->{
            app.setNriDiscount(app.getBasePremium() * 0.05d);
        });
    }

    private void wellnessDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getWellnessDiscount() || !premiumRequest.getWellnessDiscount().isWellnessDiscount()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("wellnessDiscount",amountDivision, premiumRequest.getPolicyType());
        final Double discount;
        final Double points = premiumRequest.getWellnessDiscount().getPoints();
        if(751 <= points){
            discount = 0.20d;
        }

        else if(601 <= points){
            discount = 0.14d;
        }

        else if(351 <= points){
            discount = 0.10d;
        }

        else if(200 <= points){
            discount = 0.04d;
        } else {
            discount = 0.0d;
        }

        ls.forEach(app->{
            app.setWellnessDiscount(app.getBasePremium() * discount);
        });



    }

    private void medicalEquipmentCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isDurableMedicalEquipmentCover()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("medicalEquipmentCover",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app->{
            app.setDurableMedicalEquipmentCoverLoading(app.getBasePremium() * 0.1d);
        });

    }

    private void subLimitModeration(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isSubLimitsForModernTreatments()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("subLimitModeration",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app->{
            app.setSubLimitForModernTreatmentsDiscount(app.getBasePremium() * 0.05d);
        });

    }

    private void roomRentModification(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getRoomRent() || !premiumRequest.getRoomRent().isRent()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("roomRent",amountDivision, premiumRequest.getPolicyType());
        final String option =  premiumRequest.getRoomRent().getOption();
        final double discount = "general".equals(option)?0.20d:("shared".equals(option)?0.10d:0.05d);
        ls.forEach(app->{
            app.setRoomRentDiscount(app.getBasePremium() * discount);
        });
    }

    private void voluntarilyDeductible(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getVoluntarilyDeductible() || !premiumRequest.getVoluntarilyDeductible().isDeductible()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("deductible",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app->{
            app.setDeductibleDiscount(app.getBasePremium()*DynamicConfigurations.getVoluntaryDeductiblePercent(app.getAge(), Integer.parseInt(premiumRequest.getVoluntarilyDeductible().getDeductibleAmount())));
        });
    }

    private void voluntarilyCopay(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        final List<String> copayLs = Arrays.asList("10","20","30","40","50");
        if(null == premiumRequest.getVoluntarilyCopay() ||
                !premiumRequest.getVoluntarilyCopay().isCopay() ||
                !copayLs.contains(premiumRequest.getVoluntarilyCopay().getCopayPercent())){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("copay",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app->app.setCopayDiscount(app.getBasePremium()*(Double.parseDouble(premiumRequest.getVoluntarilyCopay().getCopayPercent()))/100.0d));
    }

    private void preferredHospitalNetwork(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isPreferredHospital()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("preferredhospitalNetwork",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app->app.setPreferredHospitalDiscount(app.getBasePremium()*0.10d));
    }

    private void infiniteCare(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isInfiniteCare()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("infiniteCare",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(
                app-> {
                    app.setInfiniteCareLoading(app.getBasePremium()* this.dynamicConfigurations.getInfiniteCare(premiumRequest.getSumInsured()));
                }
        );
    }

    private void pedWaitingPeriod(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(null == premiumRequest.getPedWaitingRequest() || !premiumRequest.getPedWaitingRequest().isPedWaitingRequest()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("pedWaitingPeriod",amountDivision, premiumRequest.getPolicyType());
        final Optional<Integer> maxAge = PremiumService.getMaxAge(ls);
        final int age = maxAge.orElse(0);
        final Double value = DynamicConfigurations.getReductionOfPEDWaitingPercent(age, premiumRequest.getPedWaitingRequest().getWaitingPeriod());
        ls.forEach(app -> {
            app.setPedWaitingPeriodLoading(app.getBasePremium()*
                    value);
        });
    }

    private void specificDisease(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        final List<Applicable> ls = this.determineConfiguration("specificDisease",amountDivision, premiumRequest.getPolicyType());
        /* TODO make it generic */
        final Optional<Integer> maxAge = PremiumService.getMaxAge(ls);
        final int age = maxAge.orElse(0);
        final double loading = DynamicConfigurations.getSpecificDiseaseConf(age);
        ls.forEach(app->app.setSpecificDiseaseLoading(app.getBasePremium()*loading));
    }

    private static Optional<Integer> getMaxAge(final List<Applicable> ls) {
        return ls.stream()
                .map(Applicable::getAge)
                .max(Integer::compareTo);
    }

    private void futureReady(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isFutureReady()){
            return ;
        }

        final List<Applicable> ls = this.determineConfiguration("futureReady",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> app.setFutureReadyLoading(app.getBasePremium()*DynamicConfigurations.getFutureReadyconf(app.getAge())));
    }

    private void consumableCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isConsumableCover())
            return ;

        final List<Applicable> ls = this.determineConfiguration("consumableCover",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app->app.setConsumableCoverLoading(app.getBasePremium()*0.10d));
    }

    private void instantCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        final Set<String> masterDiseases = Set.of("BP", "DM", "CAD", "Asthma", "Hyperlipedimia");
        final List<Applicable> applicables = this.determineConfiguration("instantCover",amountDivision, premiumRequest.getPolicyType());

        for (final Applicable app : applicables) {
            final boolean isMasterDisease = app.getPeds().stream().anyMatch(masterDiseases::contains);
            final boolean isCad = app.getPeds().contains("CAD");

            final double loading = isCad ? app.getBasePremium() * 0.30
                    : (isMasterDisease ? app.getBasePremium() * 0.20
                    : (app.getPeds().isEmpty() ? 0 : app.getBasePremium() * 0.15));

            app.setInstantCoverLoading(loading);
        }
    }


    private void applyPowerBooster(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        if(!premiumRequest.isPowerBooster())
            return ;
        final List<Applicable> ls = this.determineConfiguration("powerbooster",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> {
            app.setPowerBoosterLoading(app.getPowerBoosterLoading() + app.getBasePremium()* this.dynamicConfigurations.getPowerBooster(premiumRequest.getSumInsured()));
        });
    }

    private void stage1(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        this.lookup(amountDivision, premiumRequest);
        this.zoneDiscount(amountDivision, premiumRequest);
        this.floaterDiscount(amountDivision, premiumRequest);
        this.reflexLoading(amountDivision, premiumRequest);
        amountDivision.getApplicables().forEach(Applicable::prepareStageISum);
    }

    private void reflexLoading(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        final List<Applicable> ls = this.determineConfiguration("reflexLoading",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(app -> app.setReflexLoadingExpense(app.getReflexLoadingExpense() + app.getBasePremium()*app.getReflexLoadingPercentage()));
    }

    private void floaterDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        final List<Applicable> ls = this.determineConfiguration("floater",amountDivision, premiumRequest.getPolicyType());
        final double discount = "floater".equals(premiumRequest.getPolicyType()) ? 0.20d : 0.0d ;
        ls.forEach(app -> {
            app.setPolicyTypeDiscount(app.getPolicyTermDiscount() + app.getBasePremium()*discount);
            app.setBasePremium(app.getBasePremium() - app.getPolicyTypeDiscount());
        });
    }

    private void zoneDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        final List<Applicable> ls = this.determineConfiguration("zonalDiscount",amountDivision, premiumRequest.getPolicyType());
        final double discount;
        if("B".equals(premiumRequest.getZone())){
            discount = 0.17d;
        }

        else if("C".equals(premiumRequest.getZone())){
            discount = 0.30d;
        } else {
            discount = 0.0d;
        }

        ls.forEach(app -> {
            app.setZoneDiscount(app.getZoneDiscount() + app.getBasePremium()*discount);
            app.setBasePremium(app.getBasePremium() - app.getZoneDiscount());
        });
    }

    private void lookup(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        final List<Applicable> ls = this.determineConfiguration("lookup",amountDivision, premiumRequest.getPolicyType());
        ls.forEach(applicable -> applicable.setBasePremium(this.premiumConfig.getPremium(applicable.getAge(), applicable.getType(), premiumRequest.getSumInsured())));
    }

    private List<Applicable> determineConfiguration(final String config, final AmountDivision amountDivision, final String policyType) {
        try {
            return Utils.get(this.mandatoryConfiguration.getConf(config, policyType), amountDivision.getApplicables());
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}