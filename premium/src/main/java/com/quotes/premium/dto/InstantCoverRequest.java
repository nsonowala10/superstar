package com.quotes.premium.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class InstantCoverRequest implements Serializable {
    private boolean instantCover;
    private List<String> disease;
}
