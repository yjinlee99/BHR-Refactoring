package com.nineties.bhr.global.security.jwt;

import com.nineties.bhr.employee.domain.Employees;
import com.nineties.bhr.employee.domain.Role;
import com.nineties.bhr.employee.dto.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return "/login".equals(path)
                || "/api/auth/refresh".equals(path)
                || "/api/auth/logout".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        // Access Token이 없는 요청
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorization.substring(7);

        try {

            // 일반 API에서는 Access Token만 허용
            if (!"access".equals(
                    jwtUtil.getCategory(token)
            )) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                return;
            }

            String username =
                    jwtUtil.getUsername(token);

            String roleStr =
                    jwtUtil.getRole(token);

            String empId =
                    jwtUtil.getEmpId(token);

            Role role =
                    Role.getRole(roleStr);

            if (role == null) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                return;
            }

            Employees employees =
                    new Employees();

            employees.setId(empId);
            employees.setUsername(username);
            employees.setPassword("temppassword");
            employees.setRole(role);

            CustomUserDetails customUserDetails =
                    new CustomUserDetails(employees);

            Authentication authToken =
                    new UsernamePasswordAuthenticationToken(
                            customUserDetails,
                            null,
                            customUserDetails.getAuthorities()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authToken);

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (JwtException |
                 IllegalArgumentException e) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }
    }
}