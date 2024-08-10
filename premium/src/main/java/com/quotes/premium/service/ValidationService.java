package com.quotes.premium.service;

import com.quotes.premium.dto.Insured;
import com.quotes.premium.dto.MaternityOptions;
import com.quotes.premium.dto.PremiumRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    private static final Set<String> ALLOWED_SUM_INSURED = Set.of("500000", "750000", "1000000", "1500000", "2000000", "2500000", "5000000", "10000000", "UNLIMITED");
    private static final Set<String> ALLOWED_ZONES = Set.of("A", "B", "C");
    private static final Set<String> ALLOWED_POLICY_TYPES = Set.of("individual", "floater");
    private static final int MIN_POLICY_TERM = 1;
    private static final int MAX_POLICY_TERM = 5;

    void validatePremiumRequest(final PremiumRequest premiumRequest) throws Exception {
        this.validateSumInsured(premiumRequest.getSumInsured());
        this.validatePolicyTerm(premiumRequest.getPolicyTerm());
        this.validateZone(premiumRequest.getZone());
        this.validateInsured(premiumRequest.getInsured());
        this.validatePolicyType(premiumRequest.getInsured(), premiumRequest.getPolicyType());
        this.validateVoluntarilyDeductible(premiumRequest);
        this.validateMaternity(premiumRequest);
    }

    private void validateSumInsured(final String sumInsured) {
        if (!ValidationService.ALLOWED_SUM_INSURED.contains(sumInsured)) {
            throw new RuntimeException("sum insured is wrong");
        }
    }

    private void validatePolicyTerm(final int policyTerm) {
        if (ValidationService.MIN_POLICY_TERM > policyTerm || ValidationService.MAX_POLICY_TERM < policyTerm) {
            throw new RuntimeException("policy term is wrong");
        }
    }

    private void validateZone(final String zone) {
        if (!ValidationService.ALLOWED_ZONES.contains(zone)) {
            throw new RuntimeException("zone is wrong");
        }
    }

    private void validateInsured(final List<Insured> insured) {
        if (null == insured) {
            throw new RuntimeException("family size is wrong");
        }
        final long adultCount = insured.stream().filter(ins -> "adult".equals(ins.getType())).count();
        final long childCount = insured.stream().filter(ins -> "child".equals(ins.getType())).count();
        if (2 < adultCount || 4 < childCount) {
            throw new RuntimeException("family size is wrong");
        }
    }

    private void validatePolicyType(final List<Insured> insured, final String policyType) {
        if (!ValidationService.ALLOWED_POLICY_TYPES.contains(policyType)) {
            throw new RuntimeException("policy type is wrong");
        }
        if ("floater".equals(policyType) && 1 >= (long) insured.size()) {
            throw new RuntimeException("floater must have more than one insured");
        }
    }

    private void validateVoluntarilyDeductible(final PremiumRequest premiumRequest) {
        if (null != premiumRequest.getVoluntarilyDeductible()
                && premiumRequest.getVoluntarilyDeductible().isDeductible()
                && null != premiumRequest.getVoluntarilyCopay()
                && premiumRequest.getVoluntarilyCopay().isCopay()) {
            throw new RuntimeException("copay and deductible cannot be added together");
        }
    }

    private void validateMaternity(final PremiumRequest premiumRequest) {
        if (null != premiumRequest.getMaternityRequest() && premiumRequest.getMaternityRequest().isMaternityRequest()) {
            final List<MaternityOptions> options = premiumRequest.getMaternityRequest().getOption();
            final Set<String> selectedOptions = options.stream()
                    .map(MaternityOptions::getOption)
                    .collect(Collectors.toSet());
            if (selectedOptions.contains("A") && selectedOptions.contains("B")) {
                throw new RuntimeException("option A & B cannot be selected together");
            }
        }
    }
}