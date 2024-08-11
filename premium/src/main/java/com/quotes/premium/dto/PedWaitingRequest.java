package com.quotes.premium.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedWaitingRequest {
    private boolean isPedWaitingRequest;
    private String waitingPeriod;
}
