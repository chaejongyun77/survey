package com.woongjin.survey.global.config;

import com.woongjin.survey.global.interceptor.ClientInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 *
 * [인터셉터 등록 전략]
 *  ClientInterceptor 는 설문 참여 경로에만 적용.
 *
 *  적용 경로 (addPathPatterns)
 *  ├─ /surveys/response          — 설문 참여 View 페이지
 *  └─ /api/surveys/**            — 설문 참여 REST API (문항 조회, 응답 제출 등)
 *
 *  제외 경로 (excludePathPatterns)
 *  └─ /surveys/intro             — 일회성 Redis 토큰으로 접근, 여기서 Client JWT 를 발급하는 진입점
 *                                  → 인터셉터 적용 대상에서 반드시 제외
 *
 *  [Security 필터와의 관계]
 *   Security permitAll() 로 열려 있는 경로들 중 일부(/surveys/response, /api/surveys/**)에
 *   MVC 인터셉터로 별도 Client JWT 검증을 추가하는 구조.
 *   → Security 는 "Spring Security 인증(직원)" 을 건드리지 않고,
 *     인터셉터가 "설문 참여 인증(Client JWT)" 을 담당.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ClientInterceptor clientInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientInterceptor)
                .addPathPatterns(
                        "/surveys/response",   // 설문 참여 View
                        "/api/surveys/**"      // 설문 참여 REST API
                )
                .excludePathPatterns(
                        "/surveys/intro"       // Client JWT 발급 진입점 — 인터셉터 제외
                );
    }
}
