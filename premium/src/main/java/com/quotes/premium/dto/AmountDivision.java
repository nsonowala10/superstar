package com.quotes.premium.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AmountDivision implements Serializable {

    private Double basePremium = 0.0d;
    private Double premiumForAdults = 0.0d;
    private Double premiumForChild = 0.0d;
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
    private Double sharedRoomDiscount;
    private Double subLimitForModernTreatmentsDiscount;
    private Double durableMedicalEquipmentCoverLoading;
    private Double wellnessDiscount;
    private Double nriDiscount;


    private Double finalPremium = 0.0d;

    public void setPolicyTypeDiscount(final String policyType) {
        this.policyTypeDiscount = "floater".equals(policyType)
                ? (this.premiumForAdults * 20) / 100.0d
                : this.policyTypeDiscount;
    }

    public void setZoneDiscount(final String zone, final String sumInsured) {
        final double discount = switch (zone) {
            case "C" -> "500000".equals(sumInsured) || "750000".equals(sumInsured) ? 15.0d : 20.0d;
            case "D" -> "500000".equals(sumInsured) || "750000".equals(sumInsured) ? 25.0d : 30.0d;
            default -> throw new IllegalArgumentException("Invalid zone: " + zone);
        };
        this.setZoneDiscount((this.basePremium *discount)/100.0d);
    }

    public void initializeFinalPremium() {
        this.finalPremium = this.basePremium ;
    }

    public void operateOnBasePremium(final String type, final Double amount) {
        this.basePremium += "loading".equals(type) ? amount : -amount;
    }

    public void operateOnFinalPremium(final String type, final Double amount) {
        this.finalPremium += "loading".equals(type) ? amount : -amount;
    }

    public void setPowerBoosterLoading(final String sumInsured, final boolean powerBooster, final Map<String, Double> config){
        final double loading = powerBooster ? (this.basePremium * config.getOrDefault(sumInsured, 0.0d)) / 100.0d : 0.0d;
        this.setPowerBoosterLoading(loading);
        this.operateOnFinalPremium("loading", this.powerBoosterLoading);
    }

    public void setConsumableCoverLoading(final boolean consumableCover) {
        this.consumableCoverLoading = consumableCover ? (this.basePremium * 10) / 100.0d : 0.0d;
        this.operateOnFinalPremium("loading", this.consumableCoverLoading);
    }

    public void setFutureReadyLoading(final boolean futureReady, final List<Insured> insured) {
        if (futureReady) {
            int maxAge = insured.stream()
                    .filter(ins -> "adult".equals(ins.getType()))
                    .mapToInt(Insured::getAge)
                    .max()
                    .orElse(0);

            double loading = maxAge <= 50 ? 1.0d : maxAge <= 60 ? 0.5d : 0.0d;
            this.futureReadyLoading = this.basePremium * loading / 100.0d;
            this.operateOnFinalPremium("loading", this.futureReadyLoading);
        }
    }

    public void setSpecificDiseaseLoading(final boolean reductionOfSpecificDisease, final List<Insured> insured) {
        if (reductionOfSpecificDisease) {
            int maxAge = insured.stream()
                    .filter(ins -> "parent".equals(ins.getType()))
                    .mapToInt(Insured::getAge)
                    .max()
                    .orElse(0);

            double loading = (maxAge <= 35) ? 10d :
                    (maxAge <= 45) ? 25d :
                            (maxAge <= 50) ? 30d :
                                    (maxAge <= 55) ? 40d : 50d;

            this.setSpecificDiseaseLoading((this.basePremium * loading) / 100.0d);
            this.operateOnFinalPremium("loading", this.specificDiseaseLoading);
        }
    }

    public void setPedWaitingPeriodLoading(final boolean reductionOfPed, final List<Insured> insured) {
        if (reductionOfPed) {
            int maxAge = insured.stream()
                    .filter(ins -> "parent".equals(ins.getType()))
                    .mapToInt(Insured::getAge)
                    .max()
                    .orElse(0);

            double loading = (maxAge <= 35) ? 10d :
                    (maxAge <= 45) ? 25d :
                            (maxAge <= 50) ? 30d :
                                    (maxAge <= 55) ? 40d : 50d;

            this.pedWaitingPeriodLoading = this.basePremium * loading / 100.0d;
            this.operateOnFinalPremium("loading", this.pedWaitingPeriodLoading);
        }
    }

    public void setCopayDiscount(final VoluntarilyCopay voluntarilyCopay, final List<String> allowedValues) {
        if (voluntarilyCopay.isCopay() && allowedValues.contains(voluntarilyCopay.getCopayPercent())) {
            this.copayDiscount = this.basePremium * Double.parseDouble(voluntarilyCopay.getCopayPercent()) / 100.0d;
            this.operateOnFinalPremium("discount", this.copayDiscount);
        }
    }

    public void setInfiniteCareLoading(final String sumInsured, final Double toApply) {
        this.setInfiniteCareLoading(this.basePremium *toApply);
        this.operateOnFinalPremium("loading", this.infiniteCareLoading);
    }

    public void setPreferredHospitalDiscount(final boolean preferredHospital) {
        this.preferredHospitalDiscount = preferredHospital ? this.basePremium * 0.1d : 0.0d;
        this.operateOnFinalPremium("discount", this.preferredHospitalDiscount);
    }

    public void setInstantCoverLoading(final boolean instantCover){
        this.instantCoverLoading = instantCover ? this.basePremium * 0.3d : 0.0d;
    }

    public void setSharedRoomDiscount(final boolean sharedRoom){
        this.sharedRoomDiscount = sharedRoom ? this.basePremium * 0.1d : 0.0d;
    }

    public void setSubLimitForModernTreatmentsDiscount(final boolean subLimitForModernTreatments){
        this.subLimitForModernTreatmentsDiscount = subLimitForModernTreatments ? this.basePremium * 0.05d : 0.0d;
    }

    public void setDurableMedicalEquipmentCoverLoading(final boolean durableMedicalEquipmentCover){
        this.durableMedicalEquipmentCoverLoading = durableMedicalEquipmentCover ? this.basePremium * 0.1d : 0.0d;
    }

    public void setWellnessDiscount(final boolean wellnessCover){
        this.wellnessDiscount = wellnessCover ? this.basePremium * 0.1d : 0.0d;
    }

    public void setNriDiscount(final boolean nriCover){
        this.wellnessDiscount = nriCover ? this.basePremium * 0.1d : 0.0d;
    }


}
