package com.woongjin.survey.global.security;

import com.woongjin.survey.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 (JWT 기반)
 * - 세션 사용 안 함 (STATELESS)
 * - JwtAuthenticationFilter로 매 요청마다 토큰 검증
 * - formLogin 제거, 로그인은 AuthController에서 직접 처리
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈 등록
     * - AuthService에서 비밀번호 검증 시 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1) CSRF 비활성화 — JWT는 쿠키 기반이 아닌 토큰 기반이라 CSRF 불필요
            .csrf(csrf -> csrf.disable())

            // 2) X-Frame-Options 설정
            //    - Spring Security 기본 frameOptions 비활성화 후 커스텀 처리
            //    - /surveys/** 경로는 8081 iframe에서 열려야 하므로 ALLOWALL
            //    - 나머지는 SAMEORIGIN 유지
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())  // 기본값 비활성화
                .addHeaderWriter((request, response) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/surveys/")) {
                        response.setHeader("X-Frame-Options", "ALLOWALL");
                    } else {
                        response.setHeader("X-Frame-Options", "SAMEORIGIN");
                    }
                })
            )

            // 3) 세션 사용 안 함 — JWT로 매 요청마다 인증하니까 서버에 세션 저장 불필요
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4) URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    // 인증 페이지 + 정적 리소스
                    "/auth/login",
                    "/css/**", "/js/**", "/images/**", "/*.html",

                    // 에러 페이지
                    "/error/**",

                    // 관리자 인증 API — 로그인/로그아웃/재발급 모두 토큰 없이 접근 필요
                    "/api/external/v1/admin/auth/**",

                    // 내부 시스템 연동 API (8081 → 8080, 직원 JWT 없음)
                    "/api/internal/v1/thinkbig/surveys/**",

                    // 사용자 설문 View — ClientTokenFilter 에서 검증 (/client/intro 는 발급소라 토큰 없음)
                    "/surveys/client/**",

                    // 사용자 설문 API — ClientTokenFilter 에서 검증
                    "/api/external/v1/thinkbig/surveys/**"
                ).permitAll()

                // 나머지 모든 요청은 직원 JWT 인증 필요
                .anyRequest().authenticated()
            )

            // 5) formLogin 제거 — 로그인은 AuthController에서 직접 처리
            //    (기존: Spring Security가 POST /auth/login 자동 처리)
            //    (변경: AuthController가 POST /auth/login 직접 받아서 JWT 발급)

            // 6) 인증 실패 시 로그인 페이지로 리다이렉트
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/auth/login");
                })
            )

            // 7) JwtAuthenticationFilter를 Spring Security 필터 앞에 등록
            //    → 매 요청마다 쿠키/헤더에서 토큰 확인 → 유효하면 SecurityContext에 인증 세팅
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
