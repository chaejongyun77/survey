package com.woongjin.survey.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.global.filter.ClientTokenFilter;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서블릿 필터 등록 설정
 *
 * [@Component 대신 FilterRegistrationBean 을 사용하는 이유]
 *  @Component 를 붙이면 Spring Boot 가 필터를 모든 경로(/*) 에 자동 등록함.
 *  FilterRegistrationBean 으로 등록하면 적용 URL 패턴을 명시적으로 제한할 수 있음.
 *
 * [URL 패턴 — Servlet 스펙]
 *  /api/external/v1/thinkbig/surveys/*  → /{id}/intro, /{id}/questions, /{id}/submit 등 하위 경로 포함
 *  /surveys/client/intro 는 패턴에 포함되지 않으므로 자연스럽게 제외 (Client JWT 발급 진입점)
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final ClientTokenProvider clientTokenProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public FilterRegistrationBean<ClientTokenFilter> clientTokenFilter() {
        FilterRegistrationBean<ClientTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ClientTokenFilter(clientTokenProvider, objectMapper));
        registration.addUrlPatterns(
                "/surveys/client/response",               // 설문 참여 View
                "/api/external/v1/thinkbig/surveys/*"     // 설문 참여 REST API
        );
        registration.setOrder(1);
        return registration;
    }
}
