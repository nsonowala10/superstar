package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoluntarilyCopay {
    private boolean copay;
    private String copayPercent;
}
