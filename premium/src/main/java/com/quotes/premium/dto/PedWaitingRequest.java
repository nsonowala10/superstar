package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PedWaitingRequest {
    private boolean isPedWaitingRequest;
    private String waitingPeriod;
}
