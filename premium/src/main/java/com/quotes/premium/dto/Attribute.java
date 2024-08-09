package com.quotes.premium.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Attribute{
    private String insured; //adult, all, child
    private String year; // all, inception
    private boolean multiplicative;
    private boolean rounding;
    private String stage;
}
