package com.quotes.premium.service;

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
        this.stage4(amountDivision, premiumRequest);
        this.stage5(amountDivision);
        return amountDivision;
    }

    private void createInsuredMapping(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        for(int year =1;year < premiumRequest.getPolicyTerm();year ++){
            for(final Insured insured : premiumRequest.getInsured()){
                Applicables applicables = new Applicables();
                applicables.setAge(insured.getAge());
                applicables.setType(insured.getType());
                applicables.setYear(premiumRequest.getPolicyTerm());
                applicables.setPeds(insured.getPeds());
                applicables.setNri(insured.isNri());
                applicables.setProposer(insured.isProposer());
                applicables.setReflexLoadingPercentage(insured.getReflexLoading()); // TODO remove it
                amountDivision.getApplicables().add(applicables);
            }
        }
    }

    private void stage5(AmountDivision amountDivision) {
        Double finalPremium = 0.0;
        for(Applicables app : amountDivision.getApplicables()){
            finalPremium = finalPremium + app.getStageIVSum();
        }
        amountDivision.setFinalPremium(finalPremium);
    }

    private void stage4(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        this.longTermDiscount(amountDivision, premiumRequest);
        amountDivision.getApplicables().forEach(app -> app.prepareStageIVSum());
    }

    private void longTermDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        Map<Integer, Double> termMap = Map.of(1,0.0d,2,0.10d,3,0.125d,4,0.150d,5,0.150d);
        amountDivision.getApplicables().forEach(app -> {
            app.setLongTermDiscount(app.getLongTermDiscount() + app.getStageIIISum()*termMap.get(app.getYear()));
        });
    }

    private void stage3(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        this.healthQuestionnaire(amountDivision, premiumRequest);
        this.cibilDiscount(amountDivision, premiumRequest);
        this.earlyRenewalDiscount(amountDivision, premiumRequest);
        amountDivision.getApplicables().forEach(app -> app.prepareStageIIISum());

        /* TODO capping */
    }

    private void earlyRenewalDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isEarlyRenewalDiscount()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("earlyRenewal"),amountDivision.getApplicables());
        ls.forEach(app -> app.setEarlyRenewalDiscount(app.getEarlyRenewalDiscount() + app.getStageIISum()*0.025d));
    }

    private void cibilDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null ==  premiumRequest.getCibilScoreRequest() || !premiumRequest.getCibilScoreRequest().isCibil()){
            return ;
        }

        int cibil = premiumRequest.getCibilScoreRequest().getCibilScore();
        Double discount;
        if(cibil >= 801){
            discount = 0.075d;
        }

        else if(cibil >= 751){
            discount = 0.05d;
        }

        else if(cibil >= 701){
            discount = 0.025d;
        } else {
            discount = 0.0d;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("cibilDiscount"),amountDivision.getApplicables());
        ls.forEach(app -> app.setCibilDiscount(app.getCibilDiscount() + app.getStageIISum()*discount));
    }

    private void healthQuestionnaire(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isHealthQuestionnaire()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("healthQuestionnaire"),amountDivision.getApplicables());
        ls.forEach(app -> app.setHealthQuestionnaireDiscount(app.getHealthQuestionnaireDiscount() + app.getStageIISum()*0.1d));
    }

    private void stage2(AmountDivision amountDivision, PremiumRequest premiumRequest) {
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
        amountDivision.getApplicables().forEach(app -> app.prepareStageIISum());
        return ;
    }

    private void paCover(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getPaCoverRequest() || !premiumRequest.getPaCoverRequest().isPaCover()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("paCover"),amountDivision.getApplicables());
        String option = premiumRequest.getPaCoverRequest().getOption();
        Double perMile = "1".equals(option) ? 1.0d : 2.0d;
        Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        Double perMileExpense = sumInsured * perMile / 1000.0d;
        ls.forEach(app -> app.setPaCoverExpense(app.getPaCoverExpense() + perMileExpense));
    }

    private void hospitalCash(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getHospitalCashRequest() || !premiumRequest.getHospitalCashRequest().isHospitalCash()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("hospitalCash"),amountDivision.getApplicables());
        ls.forEach(app -> {
            final Double expense = DynamicConfigurations.getHospitalCash(premiumRequest.getPolicyType(), app.getAge(), premiumRequest.getHospitalCashRequest().getNumberOfDays());
            app.setHospitalCashExpense(app.getHospitalCashExpense() + expense);
        });
    }

    private void compassionateVisit(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isCompassionateVisit()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("CompassionateVisit"),amountDivision.getApplicables());
        Double expense = "floater".equals(premiumRequest.getPolicyType())? 100.0d : 50.0d;
        ls.forEach(app -> app.setCompassionateVisitExpense(app.getCompassionateVisitExpense() + expense));
    }

    private void internationalSecondOpinion(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isInternationalSecondOpinion()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("internationalSecondOpinion"),amountDivision.getApplicables());
        Double expense = "floater".equals(premiumRequest.getPolicyType())? 20.0d : 15.0d;
        ls.forEach(app -> app.setInternationalSecondOpinionExpense(app.getInternationalSecondOpinionExpense() + expense));
    }

    private void annualHealthCheckUp(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isAnnualCheckUp()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("annualHealthCheckUp"),amountDivision.getApplicables());
        Double expense = DynamicConfigurations.getAnnualCheckUp(premiumRequest.getPolicyType(),premiumRequest.getSumInsured());
        ls.forEach(app -> app.setAnnualHealthCheckUpExpense(app.getAnnualHealthCheckUpExpense() + expense));
    }

    private void highEndDiagnostic(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isHighEndDiagnostic()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("highEndDiagnostic"), amountDivision.getApplicables());
        final Double amount = "floater".equals(premiumRequest.getPolicyType()) ? 1000.0d : 750.0d ;
        ls.forEach(app -> app.setHighEndDiagnosticExpense(app.getHighEndDiagnosticExpense() + amount));
    }

    private void womenCare(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isWomenCare()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("womenCare"), amountDivision.getApplicables());
        Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        Double expense = 0.0d;
        if(sumInsured <= 2500000.0d){
            expense = 500.0d;
        }
        else{
            expense = 500.0d;
        }
        Double finalExpense = expense;
        ls.forEach(app -> app.setWomenCareExpense(app.getWomenCareExpense() + finalExpense));
    }

    private void maternity(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getMaternityRequest() || !premiumRequest.getMaternityRequest().isMaternityRequest()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("maternityExpense"),amountDivision.getApplicables());
        List<MaternityOptions> maternityOptions = premiumRequest.getMaternityRequest().getOption();
        Double sumInsured = Double.valueOf(premiumRequest.getSumInsured());
        ls.forEach(app -> {
            applyMaterityExpense(maternityOptions, sumInsured, app);
        });

    }

    private static void applyMaterityExpense(List<MaternityOptions> maternityOptions, Double sumInsured, Applicables applicable) {
        for(MaternityOptions option : maternityOptions){
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
                case "c":{
                    if(sumInsured == 500000 || sumInsured == 750000){
                        newBornAmount = 10000.0d;
                    }

                    else if(sumInsured == 1000000 || sumInsured == 1500000 || sumInsured == 2000000 || sumInsured == 2500000){
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

    private void nriDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("wellnessDiscount"),amountDivision.getApplicables());
        if(ls.stream().noneMatch(app ->!app.isNri())){
            return ;
        }

        ls.forEach(app->{
            app.setNriDiscount(app.getBasePremium() * 0.05d);
        });
    }

    private void wellnessDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getWellnessDiscount() || !premiumRequest.getWellnessDiscount().isWellnessDiscount()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("wellnessDiscount"),amountDivision.getApplicables());
        Double discount;
        Double points = premiumRequest.getWellnessDiscount().getPoints();
        if(points >= 751){
            discount = 0.20d;
        }

        else if(points >= 601){
            discount = 0.14d;
        }

        else if(points >= 351){
            discount = 0.10d;
        }

        else if(points >= 200){
            discount = 0.04d;
        } else {
            discount = 0.0d;
        }

        ls.forEach(app->{
            app.setWellnessDiscount(app.getBasePremium() * discount);
        });



    }

    private void medicalEquipmentCover(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isDurableMedicalEquipmentCover()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("medicalEquipmentCover"),amountDivision.getApplicables());
        ls.forEach(app->{
            app.setDurableMedicalEquipmentCoverLoading(app.getBasePremium() * 0.1d);
        });

    }

    private void subLimitModeration(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isSubLimitsForModernTreatments()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("subLimitModeration"),amountDivision.getApplicables());
        ls.forEach(app->{
            app.setSubLimitForModernTreatmentsDiscount(app.getBasePremium() * 0.05d);
        });

    }

    private void roomRentModification(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getRoomRent() || !premiumRequest.getRoomRent().isRoomRent()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("roomRent"),amountDivision.getApplicables());
        String option =  premiumRequest.getRoomRent().getOption();
        double discount = option.equals("general")?0.20d:(option.equals("shared")?0.10d:0.05d);
        ls.forEach(app->{
            app.setRoomRentDiscount(app.getBasePremium() * discount);
        });
    }

    private void voluntarilyDeductible(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getVoluntarilyDeductible() || !premiumRequest.getVoluntarilyDeductible().isDeductible()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("deductible"),amountDivision.getApplicables());
        ls.forEach(app->{
            app.setDeductibleDiscount(app.getBasePremium()*DynamicConfigurations.getVoluntaryDeductiblePercent(app.getAge(), Integer.valueOf(premiumRequest.getVoluntarilyDeductible().getDeductibleAmount())));
        });
    }

    private void voluntarilyCopay(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<String> copayLs = Arrays.asList("10","20","30","40","50");
        if(null == premiumRequest.getVoluntarilyCopay() ||
                !premiumRequest.getVoluntarilyCopay().isCopay() ||
                !copayLs.contains(premiumRequest.getVoluntarilyCopay().getCopayPercent())){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("copay"),amountDivision.getApplicables());
        ls.forEach(app->app.setCopayDiscount(app.getBasePremium()*(Double.valueOf(premiumRequest.getVoluntarilyCopay().getCopayPercent()))/100.0d));
    }

    private void preferredHospitalNetwork(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isPreferredHospital()){
            return ;
        }
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("preferredhospitalNetwork"),amountDivision.getApplicables());
        ls.forEach(app->app.setPreferredHospitalDiscount(app.getBasePremium()*0.10d));
    }

    private void infiniteCare(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("infiniteCare"),amountDivision.getApplicables());
        ls.forEach(
                app-> {
                    app.setInfiniteCareLoading(app.getBasePremium()*dynamicConfigurations.getInfiniteCare(premiumRequest.getSumInsured()));
                }
        );
    }

    private void pedWaitingPeriod(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(null == premiumRequest.getPedWaitingRequest() || !premiumRequest.getPedWaitingRequest().isPedWaitingRequest()){
            return ;
        }

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("pedWaitingPeriod"),amountDivision.getApplicables());
        ls.forEach(app -> {
            app.setPedWaitingPeriodLoading(app.getBasePremium()*
            DynamicConfigurations.getReductionOfPEDWaitingPercent(app.getAge(), premiumRequest.getPedWaitingRequest().getWaitingPeriod()));
        });
    }

    private void specificDisease(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("specificDisease"),amountDivision.getApplicables());
        ls.forEach(app->app.setSpecificDiseaseLoading(app.getBasePremium()*DynamicConfigurations.getSpecificDiseaseConf(app.getAge())));
    }

    private void futureReady(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("futureReady"),amountDivision.getApplicables());
        ls.forEach(app -> app.setFutureReadyLoading(app.getBasePremium()*DynamicConfigurations.getFutureReadyconf(app.getAge())));
    }

    private void consumableCover(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isConsumableCover())
            return ;

        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("consumableCover"), amountDivision.getApplicables());
        ls.forEach(app->app.setConsumableCoverLoading(app.getBasePremium()*0.10d));
    }

    private void instantCover(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        Set<String> masterDiseases = Set.of("BP", "DM", "CAD", "Asthma", "Hyperlipedimia");
        List<Applicables> applicables = Utils.get(mandatoryConfiguration.getConf("instantCover"), amountDivision.getApplicables());

        for (Applicables app : applicables) {
            boolean isMasterDisease = app.getPeds().stream().anyMatch(masterDiseases::contains);
            boolean isCad = app.getPeds().contains("CAD");

            double loading = isCad ? app.getBasePremium() * 0.30
                    : (isMasterDisease ? app.getBasePremium() * 0.20
                    : (app.getPeds().isEmpty() ? 0 : app.getBasePremium() * 0.15));

            app.setInstantCoverLoading(loading);
        }
    }


    private void applyPowerBooster(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        if(!premiumRequest.isPowerBooster())
            return ;
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("powerbooster"), amountDivision.getApplicables());
        ls.forEach(app -> {
            app.setPowerBoosterLoading(app.getPowerBoosterLoading() + app.getBasePremium()* dynamicConfigurations.getPowerBooster(premiumRequest.getSumInsured()));
        });
    }



    private static String getKey(Insured insured) {
        return insured.getAge() + "#" + insured.getType();
    }

    private void stage1(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {
        this.lookup(amountDivision, premiumRequest);
        this.zoneDiscount(amountDivision, premiumRequest);
        this.floaterDiscount(amountDivision, premiumRequest);
        this.reflexLoading(amountDivision, premiumRequest);
        amountDivision.getApplicables().forEach(app -> app.prepareStageISum());
    }

    private void reflexLoading(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("reflexLoading"),amountDivision.getApplicables());
        ls.forEach(app -> app.setReflexLoadingExpense(app.getReflexLoadingExpense() + app.getBasePremium()*app.getReflexLoadingPercentage()));
    }

    private void floaterDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("floater"),amountDivision.getApplicables());
        final double discount = "floater".equals(premiumRequest.getPolicyType()) ? 0.20d : 0.0d ;
        ls.forEach(app -> app.setPolicyTypeDiscount(app.getPolicyTermDiscount() + app.getBasePremium()*discount));
    }

    private void zoneDiscount(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("zonalDiscount"),amountDivision.getApplicables());
        double discount;
        if("B".equals(premiumRequest.getZone())){
            discount = 0.17d;
        }

        else if("C".equals(premiumRequest.getZone())){
            discount = 0.30d;
        } else {
            discount = 0.0d;
        }

        ls.forEach(app -> app.setZoneDiscount(app.getZoneDiscount() + app.getBasePremium()*discount));
    }

    private void lookup(AmountDivision amountDivision, PremiumRequest premiumRequest) {
        List<Applicables> ls = Utils.get(mandatoryConfiguration.getConf("lookup"), amountDivision.getApplicables());
        ls.forEach(applicable -> applicable.setBasePremium(this.premiumConfig.getPremium(applicable.getAge(), applicable.getType(), premiumRequest.getSumInsured())));
    }

    private void addRiskBasedLoading(final List<Insured> insured) {
        final List<Insured> adults = insured
                .stream()
                .filter(ins-> "adult".equals(ins.getType()))
                .toList();

        final Optional<Insured> optionalMax = adults
                .stream()
                .max(Comparator.comparingInt(Insured::getAge));

        final Insured maxInsured = optionalMax.orElse(null);
        assert null != maxInsured;
        maxInsured.setBasePremium(maxInsured.getBasePremium() + maxInsured.getBasePremium()*30/100.0d);

        final Optional<Insured> otherAdultOptional = adults
                .stream()
                .filter(ins -> !ins.equals(maxInsured))
                .findFirst();

        final Insured otherAdult = otherAdultOptional.orElse(null);
        assert null != otherAdult;
        otherAdult.setBasePremium(otherAdult.getBasePremium() + otherAdult.getBasePremium()*15/100.0d);
    }
}