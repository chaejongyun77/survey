package com.woongjin.survey.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정값 바인딩
 * - application.properties의 jwt.* 값을 자동으로 매핑
 * - 실제 값은 .env (local) 또는 OS 환경변수 (dev/prod)에서 주입
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Base64 인코딩된 시크릿 키 */
    private String secret;

    /** Access Token 만료 시간 (ms) - 기본 30분 */
    private long accessTokenExpiration;

    /** Refresh Token 만료 시간 (ms) - 기본 7일 */
    private long refreshTokenExpiration;
}
