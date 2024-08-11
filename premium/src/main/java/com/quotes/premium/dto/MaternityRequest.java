package com.quotes.premium.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaternityRequest {
    private boolean maternityRequest;
    private List<MaternityOptions> option;
}
