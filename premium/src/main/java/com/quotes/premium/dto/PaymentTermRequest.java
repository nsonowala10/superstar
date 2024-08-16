package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentTermRequest {

    private boolean emi;
    private String paymentDuration;
}
