package com.nineties.bhr.login.service;

import com.nineties.bhr.global.exception.BusinessException;
import com.nineties.bhr.global.exception.ErrorCode;
import com.nineties.bhr.login.domain.RefreshToken;
import com.nineties.bhr.login.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveOrUpdate(
            String empId,
            String token
    ) {
        refreshTokenRepository.findByEmpId(empId)
                .ifPresentOrElse(
                        refreshToken ->
                                refreshToken.updateToken(token),

                        () -> refreshTokenRepository.save(
                                new RefreshToken(empId, token)
                        )
                );
    }

    @Transactional
    public void rotate(
            String oldToken,
            String newToken
    ) {
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(oldToken)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.REFRESH_TOKEN_NOT_FOUND
                                )
                        );

        refreshToken.updateToken(newToken);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository
                .findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}