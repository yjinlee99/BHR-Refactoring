package com.nineties.bhr.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    // Access Token: 기본 30분
    private final long accessExpiration;

    // Refresh Token: 기본 7일
    private final long refreshExpiration;

    public JWTUtil(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.access-expiration:1800000}") long accessExpiration,
            @Value("${spring.jwt.refresh-expiration:604800000}") long refreshExpiration
    ) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getEmpId(String token) {
        return getClaims(token).get("empId", String.class);
    }

    // access / refresh 구분
    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }


    // Access Token 생성
    public String createAccessToken(String username, String role, String empId) {
        return createToken(
                "access",
                username,
                role,
                empId,
                accessExpiration
        );
    }

    // Refresh Token 생성
    public String createRefreshToken(String username, String role, String empId) {
        return createToken(
                "refresh",
                username,
                role,
                empId,
                refreshExpiration
        );
    }

    private String createToken(
            String category,
            String username,
            String role,
            String empId,
            long expiredMs
    ) {
        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .claim("empId", empId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}