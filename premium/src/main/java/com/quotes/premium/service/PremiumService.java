package com.quotes.premium.service;

import com.quotes.premium.config.BasePremiumConfig;
import com.quotes.premium.config.DynamicConfigurations;
import com.quotes.premium.dto.AmountDivision;
import com.quotes.premium.dto.Insured;
import com.quotes.premium.dto.PremiumRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PremiumService {

    @Autowired
    private BasePremiumConfig premiumConfig;
    @Autowired
    private ValidationService validationService;

    public AmountDivision calculatePremium(final PremiumRequest premiumRequest) throws Exception {
        this.validationService.validatePremiumRequest(premiumRequest);
        final AmountDivision amountDivision = new AmountDivision();
        this.getBasePremium(amountDivision, premiumRequest);
        amountDivision.setPowerBoosterLoading(premiumRequest.getSumInsured(),premiumRequest.isPowerBooster(), DynamicConfigurations.getPowerBoosterConfig());
        amountDivision.setConsumableCoverLoading(premiumRequest.isConsumableCover());
        amountDivision.setFutureReadyLoading(premiumRequest.isFutureReady(), premiumRequest.getInsured());
        amountDivision.setSpecificDiseaseLoading(premiumRequest.isReductionOnSpecificDisease(),premiumRequest.getInsured());
        amountDivision.setPedWaitingPeriodLoading(premiumRequest.isReductionOfPed(),premiumRequest.getInsured());
        amountDivision.setInfiniteCareLoading(premiumRequest.getSumInsured(), DynamicConfigurations.getInfiniteCareConfig());
        amountDivision.setPreferredHospitalDiscount(premiumRequest.isPreferredHospital());
        amountDivision.setCopayDiscount(premiumRequest.getVoluntarilyCopay(), DynamicConfigurations.getCopayAllowedValues());
        // not changing this as it has a complex logic
        DiscountByVoluntarilyDeductible.calculateDiscount(amountDivision, premiumRequest.getVoluntarilyDeductible(), premiumRequest.getInsured());
        amountDivision.setSharedRoomDiscount(premiumRequest.isSharedRoom());
        amountDivision.setSubLimitForModernTreatmentsDiscount(premiumRequest.isSubLimitsForModernTreatments());
        amountDivision.setDurableMedicalEquipmentCoverLoading(premiumRequest.isDurableMedicalEquipmentCover());
        amountDivision.setWellnessDiscount(premiumRequest.isWellnessDiscount());
        amountDivision.setNriDiscount(premiumRequest.isNriDiscount());

    //    amountDivision.setInstantCoverLoading(premiumRequest.isInstantCover());
        return amountDivision;
    }

    private void getBasePremium(final AmountDivision amountDivision, final PremiumRequest premiumRequest) {

        /* calculate base premium for each individual*/
        premiumRequest.getInsured().forEach(in -> {
            in.setBasePremium(this.premiumConfig.getPremium(in, premiumRequest.getSumInsured())); // look up
            in.applyingPed(); // applying ped [cad]
        });

        /* add risk based loading */
    //    this.addRiskBasedLoading(premiumRequest.getInsured());
        premiumRequest.getInsured().forEach(ins -> amountDivision.operateOnBasePremium("loading", ins.getBasePremium()));

        /* setting parents and child base premium */

        amountDivision.setPremiumForAdults(premiumRequest.getInsured().stream().filter(ins -> "parent".equals(ins.getType())).mapToDouble(Insured::getBasePremium).sum());
        amountDivision.setPremiumForChild(premiumRequest.getInsured().stream().filter(ins -> "child".equals(ins.getType())).mapToDouble(Insured::getBasePremium).sum());

        /*calculate zonal discount on base premium */
        amountDivision.setZoneDiscount(premiumRequest.getZone(), premiumRequest.getSumInsured());

        /*calculate floater discount on base premium (strictly for adults)*/
        amountDivision.setPolicyTypeDiscount(premiumRequest.getPolicyType());

        /* apply discount by zone & floater into base premium and this is the base premium for all subsequent addon*/

        amountDivision.operateOnBasePremium("discount", amountDivision.getZoneDiscount());
        amountDivision.operateOnBasePremium("discount", amountDivision.getPolicyTypeDiscount());
        amountDivision.initializeFinalPremium();
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
