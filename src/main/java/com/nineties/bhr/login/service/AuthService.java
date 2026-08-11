package com.nineties.bhr.login.service;

import com.nineties.bhr.global.exception.BusinessException;
import com.nineties.bhr.global.exception.ErrorCode;
import com.nineties.bhr.global.security.jwt.JWTUtil;
import com.nineties.bhr.login.dto.TokenResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse refresh(String refreshHeader) {

        String refreshToken =
                resolveBearerToken(refreshHeader);

        try {

            if (!"refresh".equals(
                    jwtUtil.getCategory(refreshToken)
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_REFRESH_TOKEN
                );
            }

            String username =
                    jwtUtil.getUsername(refreshToken);

            String role =
                    jwtUtil.getRole(refreshToken);

            String empId =
                    jwtUtil.getEmpId(refreshToken);

            String newAccessToken =
                    jwtUtil.createAccessToken(
                            username,
                            role,
                            empId
                    );

            String newRefreshToken =
                    jwtUtil.createRefreshToken(
                            username,
                            role,
                            empId
                    );

            refreshTokenService.rotate(
                    refreshToken,
                    newRefreshToken
            );

            return new TokenResponse(
                    newAccessToken,
                    newRefreshToken
            );

        } catch (ExpiredJwtException e) {

            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_EXPIRED
            );

        } catch (JwtException | IllegalArgumentException e) {

            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }
    }

    public void logout(String refreshHeader) {

        String refreshToken =
                resolveBearerToken(refreshHeader);

        refreshTokenService.deleteByToken(
                refreshToken
        );
    }

    private String resolveBearerToken(String header) {

        if (header == null ||
                !header.startsWith("Bearer ")) {

            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        String token = header.substring(7);

        if (token.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        return token;
    }
}