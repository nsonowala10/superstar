package com.quotes.premium.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class SuperstarException extends RuntimeException{

    private String message;
}
