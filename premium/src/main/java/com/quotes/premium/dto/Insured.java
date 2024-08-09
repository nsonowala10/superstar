package com.quotes.premium.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Insured {
    private String type; // adult or child
    private int age;
    private Set<String> peds = new HashSet<>();
    private boolean riskBasedLoading = false;
    private Double basePremium;
    private boolean instantCover; // need to ask

    private final static Set<String> masterDisease = new HashSet<String>(Arrays.asList("BP","DM","CAD","Asthma","Hyperlipedimia"));

    public void applyingPed(){
        double loading = 15.0d;
        if(!Insured.masterDisease.containsAll(this.peds)){
            throw new RuntimeException("disease given for instant cover is wrong");
        }

        if(this.peds.contains("CAD")){
            loading = 30.0d;
        }

        this.setBasePremium(this.basePremium + this.basePremium*loading/100.0d);
    }

    public void applyInstantCover(){
        this.setBasePremium(this.basePremium + this.basePremium*0.3);
    }
}
