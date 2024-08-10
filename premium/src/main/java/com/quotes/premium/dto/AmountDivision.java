package com.quotes.premium.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AmountDivision implements Serializable {
    private List<Applicable> applicables = new ArrayList<>();
    private Double finalPremium = 0.0d;

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(new ObjectMapper().writeValueAsString(AmountDivision.builder().finalPremium(240030.92)
                .build()));
    }
}


