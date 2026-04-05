package com.woongjin.survey.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 모든 요청마다 실행
 * - 쿠키 또는 Authorization 헤더에서 Access Token을 추출하여 인증 처리
 *
 * 동작 흐름:
 * 1) 토큰 추출 (쿠키 우선 → 헤더 fallback)
 * 2) 토큰 없으면 → 그냥 통과 (비인증 상태, Security가 접근 제어)
 * 3) 토큰 있으면 → 검증 → 유효하면 SecurityContext에 인증 정보 세팅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) 토큰 추출
        String token = resolveToken(request);

        // 2) 토큰이 있고 유효하면 → 인증 처리
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT 인증 성공: {}", authentication.getName());
        }

        // 3) 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 토큰 추출 (쿠키 우선, 없으면 Authorization 헤더에서)
     * - Thymeleaf 페이지 요청: 쿠키에서 추출
     * - REST API 요청: Authorization 헤더에서 추출
     */
    private String resolveToken(HttpServletRequest request) {
        // 쿠키에서 먼저 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 쿠키에 없으면 Authorization 헤더 확인
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 필터를 적용하지 않을 경로
     * - 로그인 페이지, 정적 리소스는 토큰 검증 불필요
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/h2-console");
    }
}
