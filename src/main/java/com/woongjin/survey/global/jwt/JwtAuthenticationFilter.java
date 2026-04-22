package com.woongjin.survey.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 인증 필터
 *
 * 동작 흐름:
 * 1) 토큰 추출 (쿠키 우선 → 헤더 fallback)
 * 2) 토큰 없으면 → 통과 (비인증 상태, Security가 접근 제어)
 * 3) 토큰 있으면 → 검증
 *    - 성공: SecurityContext에 인증 정보 세팅 후 통과
 *    - 실패: 401 JSON 응답 (재발급은 클라이언트 api.js 인터셉터가 담당)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public static final String ACCESS_TOKEN_COOKIE  = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 인증 성공: {}", authentication.getName());

            } catch (JwtAuthException e) {
                sendErrorResponse(response, e.getErrorCode());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, JwtErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String body = objectMapper.writeValueAsString(ApiResponse.error(errorCode.getMessage()));
        response.getWriter().write(body);

        log.warn("JWT 인증 실패 [{}]: {}", errorCode.name(), errorCode.getMessage());
    }

    private String resolveToken(HttpServletRequest request) {
        String cookieToken = CookieUtil.extract(request, ACCESS_TOKEN_COOKIE);
        if (cookieToken != null) {
            return cookieToken;
        }

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/login")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }
}
