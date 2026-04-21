package com.woongjin.survey.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.filter.ClientTokenFilter;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import com.woongjin.survey.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 (JWT 기반)
 *
 * [필터 체인 구조]
 * Order(1) clientFilterChain  — 설문 참여자 경로, ClientTokenFilter 적용
 * Order(2) employeeFilterChain — 직원 경로, JwtAuthenticationFilter 적용
 *
 * 두 체인을 분리함으로써 인증 도메인이 서로 간섭하지 않음.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ClientTokenProvider clientTokenProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 설문 참여자(Client) 전용 체인 — Order(1) 로 먼저 매칭
     *
     * - securityMatcher 로 설문 경로만 처리
     * - ClientTokenFilter 가 svy_client_token 검증 후 SecurityContext 에 empId 세팅
     * - /surveys/client/intro 는 Client JWT 발급소이므로 permitAll
     * - iframe 지원을 위해 X-Frame-Options ALLOWALL
     */
    @Bean
    @Order(1)
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        ClientTokenFilter clientTokenFilter = new ClientTokenFilter(clientTokenProvider, objectMapper);

        http
            .securityMatcher(
                "/api/external/v1/thinkbig/surveys/**",
                "/surveys/client/**"
            )
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h
                .frameOptions(f -> f.disable())
                .addHeaderWriter((req, res) -> res.setHeader("X-Frame-Options", "ALLOWALL"))
            )
            .addFilterBefore(clientTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/surveys/client/intro").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * 직원(Employee) 전용 체인 — Order(2)
     *
     * - JwtAuthenticationFilter 가 ACCESS_TOKEN 검증 후 SecurityContext 에 UserPrincipal 세팅
     * - 인증 실패 시 로그인 페이지로 리다이렉트
     */
    @Bean
    @Order(2)
    public SecurityFilterChain employeeFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(h -> h
                .frameOptions(f -> f.disable())
                .addHeaderWriter((req, res) -> res.setHeader("X-Frame-Options", "SAMEORIGIN"))
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/login",
                    "/css/**", "/js/**", "/images/**", "/*.html",
                    "/error/**",
                    "/api/external/v1/admin/auth/**",
                    "/api/internal/v1/thinkbig/surveys/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.sendRedirect("/auth/login"))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
