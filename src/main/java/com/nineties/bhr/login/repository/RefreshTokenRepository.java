package com.nineties.bhr.login.repository;

import com.nineties.bhr.login.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByEmpId(String empId);

    Optional<RefreshToken> findByToken(String token);
}