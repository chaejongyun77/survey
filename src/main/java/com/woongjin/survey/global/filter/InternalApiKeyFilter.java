package com.woongjin.survey.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 내부 시스템 API Key 검증 필터
 *
 * [적용 경로] FilterConfig 에서 FilterRegistrationBean 으로 지정
 *   /api/external/v1/admin/auth/*
 *
 * [처리 흐름]
 * 1) X-Internal-Api-Key 헤더 추출
 * 2) application.yml demo.internal-api-key 와 일치 여부 검증
 * 3) 성공 → 체인 통과
 *    실패 → 401 JSON 응답 (필터 체인 중단)
 *
 * [컨트롤러와의 역할 분리]
 *  기존 ExternalSurveyApiController 에 있던 API Key 검증을 이 필터로 이동.
 *  컨트롤러는 비즈니스 로직(surveyCreateService.issue)에만 집중.
 */
@Slf4j
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final String internalApiKey;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (!internalApiKey.equals(apiKey)) {
            log.warn("유효하지 않은 API Key: uri={}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    objectMapper.writeValueAsString(ApiResponse.error("인증 실패: 유효하지 않은 API Key"))
            );
            return;
        }

        chain.doFilter(request, response);
    }
}
