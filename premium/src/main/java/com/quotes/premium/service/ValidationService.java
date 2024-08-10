package com.quotes.premium.service;

import com.quotes.premium.dto.Insured;
import com.quotes.premium.dto.PremiumRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ValidationService {

    void validatePremiumRequest(PremiumRequest premiumRequest) throws Exception{
        validateSumInsured(premiumRequest.getSumInsured());
        validatePolicyTerm(premiumRequest.getPolicyTerm());
        validateZone(premiumRequest.getZone());
        validateInsured(premiumRequest.getInsured());
        validatePolicyType(premiumRequest.getInsured(),premiumRequest.getPolicyType());
        validateVoluntarilyDeductible(premiumRequest);
    }

    private void validateSumInsured(String sumInsured) {
        List<String> ALLOWED_VALUES = Arrays.asList("500000", "750000", "1000000", "1500000", "2000000", "2500000", "5000000", "10000000", "UNLIMITED");
        if(!ALLOWED_VALUES.contains(sumInsured))
            throw new RuntimeException("sum insured is wrong");
    }

    void validatePolicyTerm(int policyTerm){
        int min_years = 1;
        int max_years = 5;
        if(!(policyTerm >= min_years && policyTerm <= max_years)){
            throw new RuntimeException("policy term is wrong");
        }
    }

    private void validateZone(String zone) {
        List<String> ALLOWED_VALUES = Arrays.asList("A", "B", "C", "D");
        if(!ALLOWED_VALUES.contains(zone))
            throw new RuntimeException("zone is wrong");
    }

    private void validateInsured(List<Insured> insured){
        if(null == insured)
            throw new RuntimeException("family size is wrong");
        long parent = insured.stream().filter(ins -> ins.getType().equals("adult")).count();
        long child = insured.stream().filter(ins -> ins.getType().equals("child")).count();
        if(parent > 2 || child > 4)
            throw new RuntimeException("family size is wrong");
    }

    private void validatePolicyType(List<Insured> insured, String policyType){
        List<String> ALLOWED_VALUES = Arrays.asList("individual", "floater");
        if(!ALLOWED_VALUES.contains(policyType))
            throw new RuntimeException("policy type is wrong");

        long parent = insured.stream().filter(ins -> ins.getType().equals("adult")).count();
        long child = insured.stream().filter(ins -> ins.getType().equals("child")).count();
        if(policyType.equals("floater") && parent + child < 2)
            throw new RuntimeException("floater must have more than one insured");
    }

    private void validateVoluntarilyDeductible(PremiumRequest premiumRequest){
        if(null != premiumRequest.getVoluntarilyDeductible() && premiumRequest.getVoluntarilyDeductible().isDeductible()
            && null != premiumRequest.getVoluntarilyCopay() && premiumRequest.getVoluntarilyCopay().isCopay()){
            throw new RuntimeException("copay and deductible can not be added together");
        }
    }






}
