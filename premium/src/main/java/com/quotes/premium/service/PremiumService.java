package com.quotes.premium.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quotes.premium.config.BasePremiumConfig;
import com.quotes.premium.config.DynamicConfigurations;
import com.quotes.premium.config.MandatoryConfiguration;
import com.quotes.premium.dto.*;
import com.quotes.premium.operation.OperationRegistry;
import com.quotes.premium.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@Log4j2
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
        final Map<String,Attribute> confMap = this.mandatoryConfiguration.getConf(premiumRequest.getPolicyType());
        final List<String> executionKeys = this.mandatoryConfiguration.getExecutionKeys();
        final AmountDivision amountDivision = new AmountDivision();
        this.createInsuredMapping(amountDivision, premiumRequest);

        for(final String key : executionKeys){
            PremiumService.log.info("handling execution key : {} ",key);
            final Attribute attribute = confMap.get(key);
            final List<Applicable> applicables = Utils.get(this.mandatoryConfiguration.getFeature(key, premiumRequest.getPolicyType()),amountDivision.getApplicables());
            final String methodName = "handle" + PremiumService.capitalizeFirstLetter(key);
            final Method method = this.getClass().getMethod(methodName, AmountDivision.class, PremiumRequest.class, List.class);
            method.invoke(this, amountDivision, premiumRequest, applicables);
            amountDivision.getApplicables().forEach(app -> {
                this.applyRounding(app, attribute, key);
            });

            amountDivision.getApplicables().forEach(app -> {
                this.applyMultiplicative(app, attribute, key, "basePremium");
            });
        }

        return amountDivision;
    }

    public void applyRounding(final Applicable applicable, final Attribute attribute, final String key) {
        try {
            OperationRegistry.getOperation("round").apply(applicable, key,null, attribute);
        } catch (final Exception e) {
            PremiumService.log.error("error in rounding up for feature {}", key);
            throw new RuntimeException();
        }
    }

    public void applyMultiplicative(final Applicable applicable, final Attribute attribute, final String key, final String baseKey) {
        try {
            OperationRegistry.getOperation("multiplicative").apply(applicable, key,baseKey, attribute);
        } catch (final Exception e) {
            PremiumService.log.error("error in multiplicative operation up for feature {}", key);
            throw new RuntimeException();
        }
    }

    public void createInsuredMapping(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
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

    public void handleLongTermDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final Map<Integer, Double> termMap = Map.of(1,0.0d,2,0.10d,3,0.125d,4,0.150d,5,0.150d);
        applicables.forEach(app -> {
            app.setLongTermDiscount(app.getLongTermDiscount() + app.getBasePremium()*termMap.get(app.getYear()));
        });
    }

    public void handleEarlyRenewal(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isEarlyRenewalDiscount()){
            return ;
        }

        final Attribute attribute = this.mandatoryConfiguration.getFeature("earlyRenewal", premiumRequest.getPolicyType());
        applicables.forEach(app -> app.setEarlyRenewal(app.getEarlyRenewal() + app.getBasePremium()*0.025d));
    }

    public void handleCibilDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
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
        final Attribute attribute = this.mandatoryConfiguration.getFeature("cibilDiscount", premiumRequest.getPolicyType());
        applicables.forEach(app -> app.setCibilDiscount(app.getCibilDiscount() + app.getBasePremium()*discount));
    }

    public void handleHealthQuestionnaire(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isHealthQuestionnaire()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("healthQuestionnaire", premiumRequest.getPolicyType());
        applicables.forEach(app -> app.setHealthQuestionnaire(app.getHealthQuestionnaire() + app.getBasePremium()*0.1d));
    }

    public void handlePaCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getPaCoverRequest() || !premiumRequest.getPaCoverRequest().isPaCover()){
            return ;
        }

        final Attribute attribute = this.mandatoryConfiguration.getFeature("paCover", premiumRequest.getPolicyType());
        final String option = premiumRequest.getPaCoverRequest().getOption();
        final Double perMile = "1".equals(option) ? 1.0d : 2.0d;
        final Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        final Double perMileExpense = sumInsured * perMile / 1000.0d;
        final int maxAge = PremiumService.getMaxAge(applicables).orElse(0);
        applicables.forEach(app -> {
            if(app.getAge() == maxAge){
                app.setPaCover(app.getPaCover() + perMileExpense);
            }
            else{
                app.setPaCover(app.getPaCover() + perMileExpense/2.0d);
            }
        });
    }

    public void handleHospitalCash(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getHospitalCashRequest() || !premiumRequest.getHospitalCashRequest().isHospitalCash()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("hospitalCash", premiumRequest.getPolicyType());
        applicables.forEach(app -> {
            final Double expense = DynamicConfigurations.getHospitalCash(premiumRequest.getPolicyType(), app.getAge(), premiumRequest.getHospitalCashRequest().getNumberOfDays());
            app.setHospitalCash(app.getHospitalCash() + expense);
        });
    }

    public void handleCompassionateVisit(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isCompassionateVisit()){
            return ;
        }

        final Attribute attribute = this.mandatoryConfiguration.getFeature("CompassionateVisit", premiumRequest.getPolicyType());
        final Double expense = "floater".equals(premiumRequest.getPolicyType())? 100.0d : 50.0d;
        applicables.forEach(app -> app.setCompassionateVisit(app.getCompassionateVisit() + expense));
    }

    public void handleInternationalSecondOpinion(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isInternationalSecondOpinion()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("internationalSecondOpinion", premiumRequest.getPolicyType());
        final Double expense = "floater".equals(premiumRequest.getPolicyType())? 20.0d : 15.0d;
        applicables.forEach(app -> app.setInternationalSecondOpinion(app.getInternationalSecondOpinion() + expense));
    }

    public void handleAnnualHealthCheckUp(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isAnnualCheckUp()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("annualHealthCheckUp", premiumRequest.getPolicyType());
        final Double expense = DynamicConfigurations.getAnnualCheckUp(premiumRequest.getPolicyType(),premiumRequest.getSumInsured());
        applicables.forEach(app -> app.setAnnualHealthCheckUp(app.getAnnualHealthCheckUp() + expense));
    }

    public void handleHighEndDiagnostic(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isHighEndDiagnostic()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("highEndDiagnostic", premiumRequest.getPolicyType());
        final Double amount = "floater".equals(premiumRequest.getPolicyType()) ? 1000.0d : 750.0d ;
        applicables.forEach(app -> app.setHighEndDiagnostic(app.getHighEndDiagnostic() + amount));
    }

    public void handleWomenCare(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isWomenCare()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("womenCare", premiumRequest.getPolicyType());
        final double sumInsured = Double.parseDouble(premiumRequest.getSumInsured());
        double expense = 0.0d;
        if(2500000.0d >= sumInsured){
            expense = 500.0d;
        }
        else{
            expense = 500.0d;
        }
        final Double finalExpense = expense;
        applicables.forEach(app -> app.setWomenCare(app.getWomenCare() + finalExpense));
    }

    public void handleMaternityExpense(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getMaternityRequest() || !premiumRequest.getMaternityRequest().isMaternityRequest()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("maternityExpense", premiumRequest.getPolicyType());
        final List<MaternityOptions> maternityOptions = premiumRequest.getMaternityRequest().getOption();
        final Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        applicables.forEach(app -> {
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

    public void handleNriDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final Attribute attribute = this.mandatoryConfiguration.getFeature("nriDiscount", premiumRequest.getPolicyType());
        
        for(final Applicable app : applicables){
            if(!app.isNri()){
                return ;
            }
        }

        applicables.forEach(app->{
            app.setNriDiscount(app.getBasePremium() * 0.05d);
        });
    }

    public void handleWellnessDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getWellnessDiscount() || !premiumRequest.getWellnessDiscount().isWellnessDiscount()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("wellnessDiscount", premiumRequest.getPolicyType());
        
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

        applicables.forEach(app->{
            app.setWellnessDiscount(app.getBasePremium() * discount);
        });
    }

    public void handleMedicalEquipmentCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isDurableMedicalEquipmentCover()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("medicalEquipmentCover", premiumRequest.getPolicyType());

        applicables.forEach(app->{
            app.setMedicalEquipmentCover(app.getBasePremium() * 0.1d);
        });

    }

    public void handleSubLimitModeration(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isSubLimitsForModernTreatments()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("subLimitModeration", premiumRequest.getPolicyType());

        applicables.forEach(app->{
            app.setSubLimitModeration(app.getBasePremium() * 0.05d);
        });

    }

    public void handleRoomRent(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getRoomRent() || !premiumRequest.getRoomRent().isRent()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("roomRent", premiumRequest.getPolicyType());
        
        final String option =  premiumRequest.getRoomRent().getOption();
        final double discount = "general".equals(option)?0.20d:("shared".equals(option)?0.10d:0.05d);
        applicables.forEach(app->{
            app.setRoomRent(app.getBasePremium() * discount);
        });
    }

    public void handleDeductible(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getVoluntarilyDeductible() || !premiumRequest.getVoluntarilyDeductible().isDeductible()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("deductible", premiumRequest.getPolicyType());

        applicables.forEach(app->{
            app.setDeductible(app.getBasePremium()*DynamicConfigurations.getVoluntaryDeductiblePercent(app.getAge(), Integer.parseInt(premiumRequest.getVoluntarilyDeductible().getDeductibleAmount())));
        });
    }

    public void handleCopay(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final List<String> copayLs = Arrays.asList("10","20","30","40","50");
        if(null == premiumRequest.getVoluntarilyCopay() ||
                !premiumRequest.getVoluntarilyCopay().isCopay() ||
                !copayLs.contains(premiumRequest.getVoluntarilyCopay().getCopayPercent())){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("copay", premiumRequest.getPolicyType());

        applicables.forEach(app->app.setCopay(app.getBasePremium()*(Double.parseDouble(premiumRequest.getVoluntarilyCopay().getCopayPercent()))/100.0d));
    }

    public void handlePreferredHospitalNetwork(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isPreferredHospital()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("preferredhospitalNetwork", premiumRequest.getPolicyType());

        applicables.forEach(app->app.setPreferredHospitalNetwork(app.getBasePremium()*0.10d));
    }

    public void handleInfiniteCare(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isInfiniteCare()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("infiniteCare", premiumRequest.getPolicyType());

        applicables.forEach(
                app-> {
                    app.setInfiniteCare(app.getBasePremium()* this.dynamicConfigurations.getInfiniteCare(premiumRequest.getSumInsured()));
                }
        );
    }

    public void handlePedWaitingPeriod(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getPedWaitingRequest() || !premiumRequest.getPedWaitingRequest().isPedWaitingRequest()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("pedWaitingPeriod", premiumRequest.getPolicyType());
        
        final Optional<Integer> maxAge = PremiumService.getMaxAge(applicables);
        final int age = maxAge.orElse(0);
        final Double value = DynamicConfigurations.getReductionOfPEDWaitingPercent(age, premiumRequest.getPedWaitingRequest().getWaitingPeriod());
        applicables.forEach(app -> {
            app.setPedWaitingPeriod(app.getBasePremium()*
                    value);
        });
    }

    public void handleSpecificDisease(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isReductionOnSpecificDisease()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("specificDisease", premiumRequest.getPolicyType());
        
        /* TODO make it generic */
        final Optional<Integer> maxAge = PremiumService.getMaxAge(applicables);
        final int age = maxAge.orElse(0);
        final double loading = DynamicConfigurations.getSpecificDiseaseConf(age);
        applicables.forEach(app->app.setSpecificDisease(app.getBasePremium()*loading));
    }

    private static Optional<Integer> getMaxAge(final List<Applicable> ls) {
        return ls.stream()
                .map(Applicable::getAge)
                .max(Integer::compareTo);
    }

    public void handleFutureReady(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isFutureReady()){
            return ;
        }

        if(1 < premiumRequest.getInsured().stream().filter(ins -> "adult".equals(ins.getType())).count()){
            return ;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("futureReady", premiumRequest.getPolicyType());

        applicables.forEach(app -> app.setFutureReady(app.getBasePremium()*DynamicConfigurations.getFutureReadyconf(app.getAge())));
    }

    public void handleConsumableCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isConsumableCover()) {
            return;
        }
        final Attribute attribute = this.mandatoryConfiguration.getFeature("consumableCover", premiumRequest.getPolicyType());

        applicables.forEach(app->app.setConsumableCover(app.getBasePremium()*0.10d));
    }

    public void handleInstantCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final Set<String> masterDiseases = Set.of("BP", "DM", "CAD", "Asthma", "Hyperlipedimia");
        final Attribute attribute = this.mandatoryConfiguration.getFeature("instantCover", premiumRequest.getPolicyType());
        
        for (final Applicable app : applicables) {
            final boolean isMasterDisease = app.getPeds().stream().anyMatch(masterDiseases::contains);
            final boolean isCad = app.getPeds().contains("CAD");

            final double loading = isCad ? app.getBasePremium() * 0.30
                    : (isMasterDisease ? app.getBasePremium() * 0.20
                    : (app.getPeds().isEmpty() ? 0 : app.getBasePremium() * 0.15));

            app.setInstantCover(loading);
        }
    }

    public void handlePowerBooster(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isPowerBooster())
            return ;
        final Attribute attribute = this.mandatoryConfiguration.getFeature("powerbooster", premiumRequest.getPolicyType());

        applicables.forEach(app -> {
            app.setPowerBooster(app.getPowerBooster() + app.getBasePremium()* this.dynamicConfigurations.getPowerBooster(premiumRequest.getSumInsured()));
        });
    }

    public void handleReflexLoading(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final Attribute attribute = this.mandatoryConfiguration.getFeature("reflexLoading", premiumRequest.getPolicyType());

        applicables.forEach(app -> app.setReflexLoading(app.getReflexLoading() + app.getBasePremium()*app.getReflexLoadingPercentage()));
    }

    public void handleFloater(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final double discount = "floater".equals(premiumRequest.getPolicyType()) ? 0.20d : 0.0d;
        applicables.forEach(app -> {
            app.setFloater(app.getFloater() + app.getBasePremium()*discount);
        });
    }

    public void handleZonalDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final Attribute attribute = this.mandatoryConfiguration.getFeature("zonalDiscount", premiumRequest.getPolicyType());
        
        final double discount;
        if("B".equals(premiumRequest.getZone())){
            discount = 0.17d;
        }

        else if("C".equals(premiumRequest.getZone())){
            discount = 0.30d;
        } else {
            discount = 0.0d;
        }

        applicables.forEach(app -> {
            app.setZonalDiscount(app.getZonalDiscount() + app.getBasePremium()*discount);
        });
    }

    public void handleLookup(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {

        final Attribute attribute = this.mandatoryConfiguration.getFeature("lookup", premiumRequest.getPolicyType());

        applicables.forEach(applicable -> applicable.setLookup(this.premiumConfig.getPremium(applicable.getAge(), applicable.getType(), premiumRequest.getSumInsured())));
    }

    public void handleStageIIPremium(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables){
        amountDivision.getApplicables().forEach(
                Applicable::handleStageIIPremium
        );
    }

    public void handleStageIIIPremium(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables){
        amountDivision.getApplicables().forEach(
                Applicable::handleStageIIIPremium
        );
    }

    public void handleStageVPremium(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables){
        final Double[] finalPremium = {0.0d};
        amountDivision.getApplicables().forEach(app -> {
            finalPremium[0] = finalPremium[0] + app.getBasePremium();
        });
        amountDivision.setFinalPremium(finalPremium[0]);
    }

    private static String capitalizeFirstLetter(final String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}