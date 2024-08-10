package com.quotes.premium.dto;

import com.quotes.premium.utils.Utils;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AmountDivision implements Serializable {
    private List<Applicables> applicables;
    private Double finalPremium = 0.0d;
}
