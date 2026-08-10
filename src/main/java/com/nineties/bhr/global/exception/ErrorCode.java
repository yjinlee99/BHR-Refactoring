package com.nineties.bhr.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BADGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "BADGE_NOT_FOUND",
            "배지를 찾을 수 없습니다."
    ),

    MISSING_REQUEST_PARAMETER(
            HttpStatus.BAD_REQUEST,
            "MISSING_REQUEST_PARAMETER",
            "필수 요청 파라미터가 누락되었습니다."
    ),

    INVALID_REQUEST_BODY(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST_BODY",
            "요청 본문을 읽을 수 없습니다."
    ),

    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_FAILED",
            "입력값을 확인해 주세요."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
