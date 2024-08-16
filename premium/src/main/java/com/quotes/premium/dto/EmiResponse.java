package com.quotes.premium.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmiResponse {
    private boolean emi;
    private Double amount ;
}
