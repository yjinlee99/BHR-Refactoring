package com.nineties.bhr.global.security;

import com.nineties.bhr.global.security.jwt.JWTFilter;
import com.nineties.bhr.global.security.jwt.JWTUtil;
import com.nineties.bhr.global.security.jwt.LoginFilter;
import com.nineties.bhr.login.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil,
                          RefreshTokenService refreshTokenService) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    @Bean
    public BCryptPasswordEncoder bCrpytPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration configuration = new CorsConfiguration();

                                configuration.setAllowedOrigins(Collections.singletonList("https://9-1379.store"));
                                configuration.setAllowedMethods(Collections.singletonList("*"));
                                configuration.setAllowCredentials(true);
                                configuration.setAllowedHeaders(Collections.singletonList("*"));
                                configuration.setMaxAge(3600L);

                                configuration.setExposedHeaders(
                                        List.of(
                                                "Authorization",
                                                "Refresh-Token"
                                        )
                                );

                                return configuration;
                            }
                        }));
        //csrf disable
        http
                .csrf((auth) -> auth.disable());
        //form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());
        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());
        //경로별 인가 작업
        http
                .authorizeHttpRequests(auth -> auth

                        // 공개
                        .requestMatchers(
                                "/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/uploads/**",
                                "/static/**"
                        ).permitAll()

                        // 관리자
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/join/**",
                                "/employees/**",
                                "/annualTotal/**",
                                "/status/**"
                        ).hasAnyRole("MANAGER", "HRMANAGER")

                        // 그 외 API
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        // 로그인되지 않은 사용자
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))

                        // 로그인했지만 권한 부족
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN))
                );
        http
                .addFilterAt(new JWTFilter(jwtUtil), LoginFilter.class);

        http
                .addFilterAt(
                        new LoginFilter(
                                authenticationManager(authenticationConfiguration),
                                jwtUtil,
                                refreshTokenService
                        ),
                        UsernamePasswordAuthenticationFilter.class
                );

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();

    }
}
