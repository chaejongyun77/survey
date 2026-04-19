package com.woongjin.survey.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 설문 참여자(Client) 전용 토큰 설정값 바인딩
 * - application.properties 의 client.* 값을 매핑
 * - 실제 값은 .env (local) 또는 OS 환경변수 (dev/prod)에서 주입
 *
 * [설계 의도]
 *  - 직원용 JwtProperties 와 prefix 분리 → 시크릿 키를 완전히 다른 값으로 유지
 *  - 설문 참여 토큰은 만료시간 단일 값만 필요 (refresh 없음)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "client")
public class ClientProperties {

    /** Base64 인코딩된 시크릿 키 (JWT_SECRET 과 반드시 다른 값) */
    private String secret;

    /** Client Token 만료 시간 (ms) */
    private long tokenExpiration;
}
