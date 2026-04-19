package com.woongjin.survey.global.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.response.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 설문 참여자(Client) 전용 인터셉터
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  JwtAuthenticationFilter 와의 역할 분리                       │
 * │                                                              │
 * │  JwtAuthenticationFilter                                     │
 * │   └─ Spring Security 필터 — 직원 ACCESS_TOKEN 검증           │
 * │       대상: 관리자 페이지, 내부 API 등                        │
 * │                                                              │
 * │  ClientInterceptor  (이 클래스)                               │
 * │   └─ Spring MVC 인터셉터 — svy_client_token 검증             │
 * │       대상: /surveys/response, /api/surveys/**               │
 * │       → Security 필터와 독립 동작, 설문 경로만 적용          │
 * └──────────────────────────────────────────────────────────────┘
 *
 * [처리 흐름]
 * 1) 쿠키에서 svy_client_token 추출
 * 2) ClientTokenProvider.getClaims() 로 서명 검증 + 파싱
 * 3) 성공 → empNo / surveyId 를 request attribute 에 주입
 *    실패 → View 요청: error/invalid-token 리다이렉트
 *           API 요청:  401 JSON 응답
 *
 * [request attribute 키]
 *  - "clientEmpNo"   : String  — 사원번호
 *  - "clientSurveyId": Long    — 설문 ID
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientInterceptor implements HandlerInterceptor {

    private final ClientTokenProvider clientTokenProvider;
    private final ObjectMapper        objectMapper;

    /** 컨트롤러/서비스에서 attribute 키를 직접 참조할 수 있도록 상수 공개 */
    public static final String ATTR_EMP_NO = "clientEmpNo";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String token = CookieUtil.extract(request, ClientTokenProvider.COOKIE_NAME);

        if (token == null) {
            log.warn("Client 토큰 없음: uri={}", request.getRequestURI());
            reject(request, response, "설문 접근 권한이 없습니다.");
            return false;
        }

        try {
            Claims claims = clientTokenProvider.getClaims(token);

            // 검증 성공 → empNo 를 attribute 에 주입 (surveyId 는 요청 파라미터로 전달)
            request.setAttribute(ATTR_EMP_NO, clientTokenProvider.extractEmpNo(claims));

            log.debug("Client 토큰 검증 성공: empNo={}", request.getAttribute(ATTR_EMP_NO));
            return true;

        } catch (JwtAuthException e) {
            log.warn("Client 토큰 검증 실패 [{}]: uri={}", e.getErrorCode(), request.getRequestURI());
            reject(request, response, e.getErrorCode().getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // private
    // ─────────────────────────────────────────────────────────────

    /**
     * 요청 종류에 따라 거부 방식을 구분
     *
     * - View 요청 (Accept: text/html) → error/invalid-token 리다이렉트
     * - API 요청 (Accept: application/json 등) → 401 JSON 응답
     */
    private void reject(HttpServletRequest request,
                        HttpServletResponse response,
                        String message) throws IOException {

        if (isApiRequest(request)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    objectMapper.writeValueAsString(ApiResponse.error(message))
            );
        } else {
            // 쿠키 만료 or 위변조 → 기존 쿠키 정리 후 에러 페이지로
            CookieUtil.deleteClientCookie(response, ClientTokenProvider.COOKIE_NAME);
            response.sendRedirect("/error/invalid-token");
        }
    }

    /**
     * API 요청 여부 판단
     * - Accept 헤더에 application/json 이 포함되면 API 요청으로 간주
     * - /api/ 경로도 API 요청으로 간주 (Accept 헤더 없는 Axios 요청 방어)
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri    = request.getRequestURI();
        return uri.startsWith("/api/")
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));
    }
}
