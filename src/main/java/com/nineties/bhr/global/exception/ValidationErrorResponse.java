package com.nineties.bhr.global.exception;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        String code,
        String message,
        Map<String, String> errors
) {
    public static ValidationErrorResponse of(
            ErrorCode errorCode,
            Map<String, String> errors
    ) {
        return new ValidationErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );
    }
}
