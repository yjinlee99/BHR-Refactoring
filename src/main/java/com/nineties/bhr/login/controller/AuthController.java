package com.nineties.bhr.login.controller;

import com.nineties.bhr.login.dto.TokenResponse;
import com.nineties.bhr.login.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_HEADER =
            "Refresh-Token";

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @RequestHeader(
                    value = REFRESH_TOKEN_HEADER,
                    required = false
            )
            String refreshHeader
    ) {

        TokenResponse tokenResponse =
                authService.refresh(refreshHeader);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokenResponse.accessToken()
                )
                .header(
                        REFRESH_TOKEN_HEADER,
                        "Bearer " + tokenResponse.refreshToken()
                )
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(
                    value = REFRESH_TOKEN_HEADER,
                    required = false
            )
            String refreshHeader
    ) {

        authService.logout(refreshHeader);

        return ResponseEntity
                .noContent()
                .build();
    }
}