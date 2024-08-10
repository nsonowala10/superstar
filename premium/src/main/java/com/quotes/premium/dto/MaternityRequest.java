package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MaternityRequest {
    private boolean maternityRequest;
    private List<MaternityOptions> option;
}
