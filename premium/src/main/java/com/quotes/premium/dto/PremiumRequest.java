package com.quotes.premium.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private boolean preferredHospital;
    private VoluntarilyDeductible voluntarilyDeductible;
    private VoluntarilyCopay voluntarilyCopay;
    private boolean infiniteCare;
    private RoomRent roomRent;
    private boolean subLimitsForModernTreatments;
    private boolean durableMedicalEquipmentCover;
    private WellnessDiscount wellnessDiscount;
    private PedWaitingRequest pedWaitingRequest;
    private MaternityRequest maternityRequest;
    private boolean womenCare;
    private boolean highEndDiagnostic;
    private boolean annualCheckUp;
    private boolean internationalSecondOpinion;
    private boolean compassionateVisit;
    private HospitalCashRequest hospitalCashRequest;
    private PACoverRequest paCoverRequest;
    private boolean healthQuestionnaire;
    private CibilScoreRequest cibilScoreRequest;
    private boolean earlyRenewalDiscount;
    private boolean bonusMaximizer;
}
