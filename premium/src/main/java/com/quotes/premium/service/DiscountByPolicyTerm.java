package com.quotes.premium.service;

import com.quotes.premium.dto.AmountDivision;

import java.util.HashMap;
import java.util.Map;

public class DiscountByPolicyTerm {

    static Map<Integer, Double> discount = new HashMap<>();

    static {
        discount.put(1,0d);
        discount.put(2,10d);
        discount.put(3,12.5d);
        discount.put(4,15d);
        discount.put(5,15d);
    }
    public static void calculateDiscount(AmountDivision amountDivision, int policyTerm){
        amountDivision.setPolicyTermDiscount((amountDivision.getBasePremium()*discount.get(policyTerm))/100.0d);
        return ;
    }

    public static Double calculateDiscount(Double basePremium, int policyTerm){
        return (basePremium*discount.get(policyTerm))/100.0d;
    }
}
