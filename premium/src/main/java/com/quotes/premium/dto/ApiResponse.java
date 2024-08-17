package com.quotes.premium.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@ToString
public class ApiResponse<T>{
    private boolean success;
    private String message;
    private Optional<T> data;

    public static <T> ApiResponse<T> buildResponse(final T response, final String message, final boolean isSuccess) {
        return ApiResponse.<T>builder()
                .success(isSuccess)
                .message(message)
                .data(Optional.ofNullable(response))
                .build();
    }

}
