package com.woongjin.survey.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.filter.ClientTokenFilter;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import com.woongjin.survey.global.jwt.JwtAuthenticationFilter;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

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

    @Bean
    public CorsConfigurationSource corsConfig() {
        CorsConfiguration config = new CorsConfiguration();

        // TODO. 나중에 허용할 외부 도메인이 생기면 추가
        config.setAllowedOrigins(List.of());
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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
            .cors(c -> c.configurationSource(corsConfig()))
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
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    /**
     * 직원(Employee) 전용 체인 — Order(2)
     *
     * - JwtAuthenticationFilter 가 ACCESS_TOKEN 검증 후 SecurityContext 에 UserPrincipal 세팅
     * - 인증 실패(401): 로그인 페이지로 리다이렉트
     * - 권한 부족(403): JSON 응답
     */
    @Bean
    @Order(2)
    public SecurityFilterChain employeeFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(c -> c.configurationSource(corsConfig()))
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
                .authenticationEntryPoint(unauthorizedEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 401 인증 실패 — 직원 체인은 Thymeleaf 기반이므로 로그인 페이지로 리다이렉트
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> response.sendRedirect("/auth/login");
    }

    // 403 권한 부족
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ApiResponse<Object> errorResponse = ApiResponse.error("해당 요청에 대한 접근 권한이 없습니다.");
            writeJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, errorResponse);
        };
    }

    private void writeJsonResponse(HttpServletResponse response, int statusCode, ApiResponse<?> apiResponse) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
