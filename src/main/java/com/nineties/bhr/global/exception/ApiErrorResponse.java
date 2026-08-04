package com.nineties.bhr.global.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse (LocalDateTime timestamp, int status, String code, String message, String path) {

    public static ApiErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                path
        );
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String path) {
        return of(errorCode, errorCode.getMessage(), path);
    }
}
