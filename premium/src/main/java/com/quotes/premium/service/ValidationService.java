package com.quotes.premium.service;

import com.quotes.premium.dto.*;
import com.quotes.premium.exception.SuperstarException;
import com.quotes.premium.utils.Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ValidationService {

    public static final Set<String> ALLOWED_SUM_INSURED = Set.of("500000", "750000", "1000000", "1500000", "2000000", "2500000", "5000000", "10000000", "UNLIMITED");
    public static final Set<String> ALLOWED_ZONES = Set.of("1", "2", "3");
    public static final Set<String> ALLOWED_POLICY_TYPES = Set.of("individual", "floater");
    public static final int MIN_POLICY_TERM = 1;
    public static final int MAX_POLICY_TERM = 5;

    void validatePremiumRequest(final PremiumRequest premiumRequest, final List<String> validationKeys) throws Exception {
        for (final String key : validationKeys) {
            ValidationService.log.info("Handling validation key: {}", key);
            final Method method = this.getClass().getMethod("validate" + Utils.capitalizeFirstLetter(key), PremiumRequest.class);
            method.invoke(this, premiumRequest);
        }
    }

    public void validateSumInsured(final PremiumRequest premiumRequest) {
        if (!ValidationService.ALLOWED_SUM_INSURED.contains(premiumRequest.getSumInsured())) {
            throw new SuperstarException("sum insured is wrong");
        }
    }

    public void validatePolicyTerm(final PremiumRequest premiumRequest) {
        if (ValidationService.MIN_POLICY_TERM > premiumRequest.getPolicyTerm() || ValidationService.MAX_POLICY_TERM < premiumRequest.getPolicyTerm()) {
            throw new SuperstarException("policy term is wrong");
        }
    }

    public void validateZone(final PremiumRequest premiumRequest) {
        if (!ValidationService.ALLOWED_ZONES.contains(premiumRequest.getZone())) {
            throw new SuperstarException("zone is wrong");
        }
    }

    public void validateInsured(final PremiumRequest premiumRequest) {
        final List<Insured> insured = premiumRequest.getInsured();
        if (null == insured) {
            throw new SuperstarException("family size is wrong");
        }
        final long adultCount = insured.stream().filter(ins -> "adult".equals(ins.getType())).count();
        final long childCount = insured.stream().filter(ins -> "child".equals(ins.getType())).count();
        if (2 < adultCount || 4 < childCount || 0 == adultCount) {
            throw new SuperstarException("family size is wrong");
        }

        if(insured.stream().filter(ins -> "adult".equals(ins.getType())).anyMatch(ins -> 18 > ins.getAge())){
            throw new SuperstarException("adult age can not be less than 18");
        }

        if(insured.stream().filter(ins -> "child".equals(ins.getType())).anyMatch(ins -> 25 < ins.getAge())){
            throw new SuperstarException("child age can not be greater than 25");
        }

        if(insured.stream().filter(ins -> "adult".equals(ins.getType())).anyMatch(ins -> 65 < ins.getAge()) && ("10000000".equals(premiumRequest.getSumInsured()) || "UNLIMITED".equals(premiumRequest.getSumInsured()))){
            throw new SuperstarException("greater than 65 year adult can not opt for sum insured");
        }
    }

    public void validatePaCover(final PremiumRequest premiumRequest) {

        final double sumInsured = Double.parseDouble(premiumRequest.getSumInsured());
        if(null != premiumRequest.getPaCoverRequest()
               && premiumRequest.getPaCoverRequest().isPaCover()
               && (1000000 > sumInsured
               || 100000000 < sumInsured)){
            throw new SuperstarException("pa cover is not allowed for sum insured chosen");
        }
    }

    public void validatePolicyType(final PremiumRequest premiumRequest) {
        if (!ValidationService.ALLOWED_POLICY_TYPES.contains(premiumRequest.getPolicyType())) {
            throw new SuperstarException("policy type is wrong");
        }
        if ("floater".equals(premiumRequest.getPolicyType()) && 1 >= (long) premiumRequest.getInsured().size()) {
            throw new SuperstarException("floater must have more than one insured");
        }
    }

    public void validateVoluntarilyDeductible(final PremiumRequest premiumRequest) {
        if (null != premiumRequest.getVoluntarilyDeductible()
                && premiumRequest.getVoluntarilyDeductible().isDeductible()
                && null != premiumRequest.getVoluntarilyCopay()
                && premiumRequest.getVoluntarilyCopay().isCopay()) {
            throw new SuperstarException("copay and deductible cannot be added together");
        }
    }

    public void validateMaternityRequest(final PremiumRequest premiumRequest) {
        if (null != premiumRequest.getMaternityRequest() && premiumRequest.getMaternityRequest().isMaternityRequest()) {
            final List<MaternityOptions> options = premiumRequest.getMaternityRequest().getOption();
            final Set<String> selectedOptions = options.stream()
                    .map(MaternityOptions::getOption)
                    .collect(Collectors.toSet());
            if (selectedOptions.contains("A") && selectedOptions.contains("B")) {
                throw new SuperstarException("option A & B cannot be selected together");
            }
        }
    }

    public void validatePaymentTerm(final PremiumRequest premiumRequest) {
        if (null != premiumRequest.getPaymentTermRequest()
                && premiumRequest.getPaymentTermRequest().isEmi()
                && 3 < premiumRequest.getPolicyTerm()) {

                throw new SuperstarException("emi is not available for greater than 3 years");
        }
    }

}