package com.woongjin.survey.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.response.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 설문 참여자(Client) 전용 필터
 *
 * [적용 경로] SecurityConfig 의 clientFilterChain 에서 관리
 *   /surveys/client/**, /api/external/v1/thinkbig/surveys/**
 *
 * [처리 흐름]
 * 1) 쿠키에서 svy_client_token 추출
 * 2) ClientTokenProvider.getClaims() 로 서명 검증 + 파싱
 * 3) 성공 → SecurityContext 에 Authentication(empId) 세팅 후 체인 통과
 *    실패 → View 요청: error/invalid-token 리다이렉트
 *           API 요청:  401 JSON 응답
 */
@Slf4j
@RequiredArgsConstructor
public class ClientTokenFilter extends OncePerRequestFilter {

    private final ClientTokenProvider clientTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // /surveys/client/intro 는 Client JWT 발급 진입점 — 토큰 없이 접근
        return request.getRequestURI().equals("/surveys/client/intro");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = CookieUtil.extract(request, ClientTokenProvider.COOKIE_NAME);

        if (token == null) {
            log.warn("Client 토큰 없음: uri={}", request.getRequestURI());
            reject(request, response, "설문 접근 권한이 없습니다.");
            return;
        }

        try {
            Claims claims = clientTokenProvider.getClaims(token);
            Long empId = clientTokenProvider.extractEmpId(claims);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(empId, null, List.of())
            );
            log.debug("Client 토큰 검증 성공: empId={}", empId);
            chain.doFilter(request, response);

        } catch (JwtAuthException e) {
            log.warn("Client 토큰 검증 실패 [{}]: uri={}", e.getErrorCode(), request.getRequestURI());
            reject(request, response, e.getErrorCode().getMessage());
        }
    }

    private void reject(HttpServletRequest request,
                        HttpServletResponse response,
                        String message) throws IOException {
        if (isApiRequest(request)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
        } else {
            CookieUtil.deleteClientCookie(response, ClientTokenProvider.COOKIE_NAME);
            response.sendRedirect("/error/invalid-token");
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }
}
