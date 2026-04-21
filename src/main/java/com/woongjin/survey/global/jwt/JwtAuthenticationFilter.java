package com.woongjin.survey.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import com.woongjin.survey.global.cookie.CookieUtil;
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
 * - 모든 요청마다 실행
 * - 쿠키 또는 Authorization 헤더에서 Access Token을 추출하여 인증 처리
 *
 * 동작 흐름:
 * 1) 토큰 추출 (쿠키 우선 → 헤더 fallback)
 * 2) 토큰 없으면 → 통과 (비인증 상태, Security가 접근 제어)
 * 3) 토큰 있으면 → getClaims() 호출 (검증 + 파싱 동시)
 *    - 성공: SecurityContext에 인증 정보 세팅 후 통과
 *    - 실패(JwtAuthException): ApiResponse로 직렬화하여 즉시 응답 (필터 체인 중단)
 *
 * [주의] GlobalExceptionHandler는 Spring MVC 레이어에만 동작하므로
 *        필터에서 발생하는 예외는 여기서 직접 처리해야 함.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 토큰이 존재할 때만 검증 및 인증 시도
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

    /**
     * JWT 예외를 ApiResponse 형태로 직렬화하여 응답
     * - 필터 체인을 중단하고 즉시 응답 반환
     */
    private void sendErrorResponse(HttpServletResponse response, JwtErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String body = objectMapper.writeValueAsString(ApiResponse.error(errorCode.getMessage()));
        response.getWriter().write(body);

        log.warn("JWT 인증 실패 [{}]: {}", errorCode.name(), errorCode.getMessage());
    }

    /**
     * 토큰 추출 (쿠키 우선, 없으면 Authorization 헤더에서)
     * - Thymeleaf 페이지 요청: 쿠키에서 추출
     * - REST API 요청: Authorization 헤더에서 추출
     */
    private String resolveToken(HttpServletRequest request) {
        // 쿠키에서 먼저 확인
        String cookieToken = CookieUtil.extract(request, ACCESS_TOKEN_COOKIE);
        if (cookieToken != null) {
            return cookieToken;
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
        return path.startsWith("/auth/login")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                // 설문 참여 경로 — ClientTokenFilter 에서 별도 검증하므로 직원 JWT 체크 불필요
                || path.startsWith("/api/surveys/")
                || path.startsWith("/surveys/");
    }
}
