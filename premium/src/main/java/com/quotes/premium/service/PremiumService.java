package com.quotes.premium.service;

import com.quotes.premium.config.BasePremiumConfig;
import com.quotes.premium.config.DynamicConfigurations;
import com.quotes.premium.config.MandatoryConfiguration;
import com.quotes.premium.dto.*;
import com.quotes.premium.exception.SuperstarException;
import com.quotes.premium.operation.OperationRegistry;
import com.quotes.premium.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
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
    @Value("${consumable.cover}")
    private Double consumableCover;
    @Value("${bonus.maximizer}")
    private Double bonusMaximizer;
    @Value("${early.renewal.discount}")
    private Double earlyRenewalDiscount;
    @Value("${health.questionnaire}")
    private Double healthQuestionnaire;
    @Value("${nri.discount}")
    private Double nriDiscount;
    @Value("${medical.equipment.cover}")
    private Double medicalEquipmentCover;
    @Value("${sublimit.moderation}")
    private Double sublimitModeration;
    @Value("${preferred.hospital.network}")
    private Double preferredHospitalNetwork;

    public ApiResponse<AmountDivision> calculatePremium(final PremiumRequest premiumRequest) {
        try{
            return ApiResponse.buildResponse(this.calculate(premiumRequest), "success", true);
        }
        catch(final InvocationTargetException e){
            return ApiResponse.buildResponse(null, ((InvocationTargetException) e).getTargetException().toString(), false);
        }
        catch(final Exception e){
            return ApiResponse.buildResponse(null, e.getMessage(), false);
        }
    }

    private AmountDivision calculate(final PremiumRequest premiumRequest) throws Exception {
        this.validationService.validatePremiumRequest(premiumRequest, this.mandatoryConfiguration.getValidationKeys());
        final Map<String, Attribute> confMap = this.mandatoryConfiguration.getConf(premiumRequest.getPolicyType(), premiumRequest.isFresh());
        final List<String> executionKeys = this.mandatoryConfiguration.getExecutionKeys();
        final AmountDivision amountDivision = new AmountDivision();
        this.createInsuredMapping(amountDivision, premiumRequest);
        for (final String key : executionKeys) {
            PremiumService.log.info("Handling execution key: {}", key);
            final Attribute attribute = confMap.get(key);
            final List<Applicable> applicables = Utils.get(this.mandatoryConfiguration.getFeature(key, premiumRequest.getPolicyType(), premiumRequest.isFresh()), amountDivision.getApplicables());
            final Method method = this.getClass().getMethod("handle" + Utils.capitalizeFirstLetter(key), AmountDivision.class, PremiumRequest.class, List.class);
            method.invoke(this, amountDivision, premiumRequest, applicables);
            amountDivision.getApplicables().forEach(app -> {
                this.applyRounding(app, attribute, key);
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

    public void handleBonusMaximizer(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isBonusMaximizer()){
            return ;
        }
        applicables.forEach(app -> {
            app.setBonusMaximizer(app.getBonusMaximizer() + app.getBasePremium()*bonusMaximizer);
        });
    }

    public void handleLongTermDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null != premiumRequest.getPaymentTermRequest() && premiumRequest.getPaymentTermRequest().isEmi()){
            return ;
        }
        applicables.forEach(app -> {
            app.setLongTermDiscount(app.getLongTermDiscount() + app.getBasePremium()*this.dynamicConfigurations.getLongTermDiscount(app.getYear()));
        });
    }

    public void handleEarlyRenewal(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isEarlyRenewalDiscount()){
            return ;
        }
        applicables.forEach(app -> app.setEarlyRenewal(app.getEarlyRenewal() + app.getBasePremium()*earlyRenewalDiscount));
    }

    public void handleCibilDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null ==  premiumRequest.getCibilScoreRequest() || !premiumRequest.getCibilScoreRequest().isCibil()){
            return ;
        }

        final int cibil = premiumRequest.getCibilScoreRequest().getCibilScore();
        final Double discount = this.dynamicConfigurations.getCibilDiscount(cibil);
        applicables.stream().filter(applicable -> 50 >= applicable.getAge()).forEach(app -> app.setCibilDiscount(app.getCibilDiscount() + app.getBasePremium()*discount));
    }

    public void handleHealthQuestionnaire(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isHealthQuestionnaire()){
            return ;
        }
        applicables.forEach(app -> app.setHealthQuestionnaire(app.getHealthQuestionnaire() + app.getBasePremium()*healthQuestionnaire));
    }

    public void handlePaCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getPaCoverRequest() || !premiumRequest.getPaCoverRequest().isPaCover()){
            return ;
        }

        final String option = premiumRequest.getPaCoverRequest().getOption();
        final Double perMile = "1".equals(option) ? 1.0d : 2.0d;
        final Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        final Double perMileExpense = sumInsured * perMile / 1000.0d;
        final int maxAge = PremiumService.getMaxAge(applicables).orElse(0);
        applicables.stream().filter(app -> 18 <= app.getAge() && 70 >= app.getAge()).forEach(app -> {
            if(app.getAge() == maxAge){
                app.setPaCover(app.getPaCover() + perMileExpense);
            }
            else{
                app.setPaCover(Math.min(app.getPaCover() + perMileExpense/2.0d, 1500000));
            }
        });
    }

    public void handleHospitalCash(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getHospitalCashRequest() || !premiumRequest.getHospitalCashRequest().isHospitalCash()){
            return ;
        }
        applicables.forEach(app -> {
            final Double expense = DynamicConfigurations.getHospitalCash(premiumRequest.getPolicyType(), app.getAge(), premiumRequest.getHospitalCashRequest().getNumberOfDays());
            app.setHospitalCash(app.getHospitalCash() + expense);
        });
    }

    public void handleCompassionateVisit(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isCompassionateVisit()){
            return ;
        }

        final Double expense = this.dynamicConfigurations.getCompassionateVisit(premiumRequest.getPolicyType());
        applicables.forEach(app -> app.setCompassionateVisit(app.getCompassionateVisit() + expense));
    }

    public void handleInternationalSecondOpinion(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isInternationalSecondOpinion()){
            return ;
        }
        final Double expense = this.dynamicConfigurations.getInternationalSecondOpinion(premiumRequest.getPolicyType());
        applicables.forEach(app -> app.setInternationalSecondOpinion(app.getInternationalSecondOpinion() + expense));
    }

    public void handleAnnualHealthCheckUp(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isAnnualCheckUp()){
            return ;
        }
        final Double expense = DynamicConfigurations.getAnnualCheckUp(premiumRequest.getPolicyType(),premiumRequest.getSumInsured());
        applicables.forEach(app -> app.setAnnualHealthCheckUp(Math.min(25000,app.getAnnualHealthCheckUp() + expense)));
    }

    public void handleHighEndDiagnostic(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isHighEndDiagnostic()){
            return ;
        }
        final Double amount = this.dynamicConfigurations.getHighEndDiagnostic(premiumRequest.getPolicyType());
        applicables.forEach(app -> app.setHighEndDiagnostic(app.getHighEndDiagnostic() + amount));
    }

    public void handleWomenCare(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isWomenCare()){
            return ;
        }
        final double sumInsured = Double.parseDouble(premiumRequest.getSumInsured());
        final double expense = this.dynamicConfigurations.getWomenCareExpense(sumInsured);

        applicables.forEach(app -> app.setWomenCare(app.getWomenCare() + expense));
    }

    public void handleMaternityExpense(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getMaternityRequest() || !premiumRequest.getMaternityRequest().isMaternityRequest()){
            return ;
        }
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
        for(final Applicable app : applicables){
            if(!app.isNri()){
                return ;
            }
        }

        applicables.forEach(app->{
            app.setNriDiscount(app.getBasePremium() * this.nriDiscount);
        });
    }

    public void handleWellnessDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getWellnessDiscount() || !premiumRequest.getWellnessDiscount().isWellnessDiscount()){
            return ;
        }

        final Double points = premiumRequest.getWellnessDiscount().getPoints();
        final Double discount = this.dynamicConfigurations.getWellnessDiscount(points);

        applicables.forEach(app->{
            app.setWellnessDiscount(app.getBasePremium() * discount);
        });
    }

    public void handleMedicalEquipmentCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isDurableMedicalEquipmentCover()){
            return ;
        }

        applicables.forEach(app->{
            app.setMedicalEquipmentCover(app.getBasePremium() * this.medicalEquipmentCover);
        });

    }

    public void handleSubLimitModeration(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isSubLimitsForModernTreatments()){
            return ;
        }
        applicables.forEach(app->{
            app.setSubLimitModeration(app.getBasePremium() * this.sublimitModeration);
        });

    }

    public void handleRoomRent(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getRoomRent() || !premiumRequest.getRoomRent().isRent()){
            return ;
        }
        final String option =  premiumRequest.getRoomRent().getOption();
        final double discount = this.dynamicConfigurations.getRoomRentDiscount(option);
        applicables.forEach(app->{
            app.setRoomRent(app.getBasePremium() * discount);
        });
    }

    public void handleDeductible(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(null == premiumRequest.getVoluntarilyDeductible() || !premiumRequest.getVoluntarilyDeductible().isDeductible()){
            return ;
        }
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
        applicables.forEach(app->app.setCopay(app.getBasePremium()*(Double.parseDouble(premiumRequest.getVoluntarilyCopay().getCopayPercent()))/100.0d));
    }

    public void handlePreferredHospitalNetwork(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isPreferredHospital()){
            return ;
        }

        applicables.forEach(app->app.setPreferredHospitalNetwork(app.getBasePremium()* this.preferredHospitalNetwork));
    }

    public void handleInfiniteCare(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isInfiniteCare()){
            return ;
        }

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
        applicables.forEach(app -> app.setFutureReady(app.getBasePremium()*DynamicConfigurations.getFutureReadyconf(app.getAge())));
    }

    public void handleConsumableCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        if(!premiumRequest.isConsumableCover()) {
            return;
        }

        applicables.forEach(app->app.setConsumableCover(app.getBasePremium()* this.consumableCover));
    }

    public void handleInstantCover(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final Set<String> masterDiseases = Set.of("BP", "DM", "CAD", "Asthma", "Hyperlipedimia");

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

        applicables.forEach(app -> {
            app.setPowerBooster(app.getPowerBooster() + app.getBasePremium()* this.dynamicConfigurations.getPowerBooster(premiumRequest.getSumInsured()));
        });
    }

    public void handleReflexLoading(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        applicables.forEach(app -> app.setReflexLoading(app.getReflexLoading() + app.getBasePremium()*app.getReflexLoadingPercentage()));
    }

    public void handleFloater(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        final double discount = this.dynamicConfigurations.getPolicyTypeDiscount(premiumRequest.getPolicyType());
        applicables.forEach(app -> {
            app.setFloater(app.getFloater() + app.getBasePremium()*discount);
        });
    }

    public void handleZonalDiscount(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {

        final double discount = this.dynamicConfigurations.getZonalDiscount(premiumRequest.getZone());

        applicables.forEach(app -> {
            app.setZonalDiscount(app.getZonalDiscount() + app.getBasePremium()*discount);
        });
    }

    public void handleLookup(final AmountDivision amountDivision, final PremiumRequest premiumRequest, final List<Applicable> applicables) {
        applicables.stream().filter(applicable -> 50 >= applicable.getYear()).forEach(applicable -> applicable.setLookup(this.premiumConfig.getPremium(applicable.getAge(), applicable.getType(), premiumRequest.getSumInsured())));
        applicables.stream().filter(applicable -> 50 < applicable.getYear()).forEach(applicable -> applicable.setLookup(this.premiumConfig.getPremium(applicable.getAge() + applicable.getYear() - 1, applicable.getType(), premiumRequest.getSumInsured())));
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
        if(null == premiumRequest.getPaymentTermRequest() || !premiumRequest.getPaymentTermRequest().isEmi()){
            return ;
        }

        PremiumService.handlePaymentTerm(amountDivision, premiumRequest);
        return ;
    }

    private static void handlePaymentTerm(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        double emiAmount = 0.0d;

        final String duration = premiumRequest.getPaymentTermRequest().getPaymentDuration();
        if("monthly".equals(duration)){
            amountDivision.setFinalPremium(amountDivision.getFinalPremium() + amountDivision.getFinalPremium()*0.04d);
            emiAmount = amountDivision.getFinalPremium() / (premiumRequest.getPolicyTerm()*12);
        }

        else if("quarterly".equals(duration)){
            amountDivision.setFinalPremium(amountDivision.getFinalPremium() + amountDivision.getFinalPremium()*0.03d);
            emiAmount = amountDivision.getFinalPremium() / (premiumRequest.getPolicyTerm()*4);
        }

        else {
            emiAmount = amountDivision.getFinalPremium() / (premiumRequest.getPolicyTerm()*2);
        }

        amountDivision.setEmiResponse(
                EmiResponse.builder()
                        .amount(emiAmount)
                        .emi(true)
                        .build());
    }

    public String test(final String var){
        return var;
    }
}