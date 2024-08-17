package com.quotes.premium.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SuperstarException extends RuntimeException{

    private String message;
}
