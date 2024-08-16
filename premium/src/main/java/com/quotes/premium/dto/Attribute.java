package com.quotes.premium.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attribute{
    private String insured; //adult, all, child
    private String year; // all, inception
    private boolean multiplicative;
    private boolean rounding;
    private String stage;
    private String expenseType;
}
