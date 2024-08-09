package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PremiumRequest implements Serializable {

    private String sumInsured;
    private int policyTerm;
    private String zone;
    private List<Insured> insured;
    private String policyType;
    private boolean powerBooster;
    private boolean instantCover;
    private boolean consumableCover;
    private boolean futureReady;
    private boolean reductionOnSpecificDisease;
    private boolean reductionOfPed;
    private boolean preferredHospital;
    private VoluntarilyDeductible voluntarilyDeductible;
    private VoluntarilyCopay voluntarilyCopay;
    private boolean infiniteCare;
    private boolean sharedRoom;
    private boolean subLimitsForModernTreatments;
    private boolean durableMedicalEquipmentCover;
    private boolean wellnessDiscount;
    private boolean nriDiscount;
}
