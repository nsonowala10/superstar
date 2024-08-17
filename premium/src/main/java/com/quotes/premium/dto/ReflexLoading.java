package com.quotes.premium.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReflexLoading {
    private String type;  // STP, NSTP
    private Float percent;
}
