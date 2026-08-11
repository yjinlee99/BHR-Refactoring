package com.nineties.bhr.login.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}