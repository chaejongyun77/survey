package com.woongjin.survey.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * 설문 참여자(Client) 전용 JWT 공급자
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │ 직원용 JwtTokenProvider 와의 분리                        │
 * │                                                         │
 * │  - secretKey 를 완전히 다른 값으로 사용                  │
 * │    (CLIENT_SECRET ≠ JWT_SECRET)                         │
 * │  - 직원 JWT 가 설문 경로에 들어와도 서명 검증에서 실패   │
 * │    → 토큰 오용 자체가 원천 차단                          │
 * │                                                         │
 * │  [클레임 구조]                                           │
 * │   sub  : empId (사원 PK)                                 │
 * └─────────────────────────────────────────────────────────┘
 *
 * [만료 전략]
 *  - client.token-expiration (CLIENT_TOKEN_EXPIRATION) 단일 값 사용
 *  - refresh 개념 없음 — 설문 세션 동안만 유효
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientTokenProvider {

    private final ClientProperties clientProperties;
    private SecretKey secretKey;

    /** 쿠키 이름 — ClientTokenFilter 와 공유 */
    public static final String COOKIE_NAME = "svy_client_token";

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(clientProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ─────────────────────────────────────────────────────────
    // 토큰 생성
    // ─────────────────────────────────────────────────────────

    /**
     * 설문 참여용 JWT 발급
     *
     * @param empId 사원 PK
     * @return 서명된 JWT 문자열
     */
    public String generateClientToken(Long empId) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + clientProperties.getTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(empId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // ─────────────────────────────────────────────────────────
    // 토큰 파싱 · 검증
    // ─────────────────────────────────────────────────────────

    /**
     * 토큰 검증 + Claims 파싱
     * - 만료        : JwtAuthException(TOKEN_EXPIRED)
     * - 위변조/형식 : JwtAuthException(TOKEN_INVALID)
     *
     * 직원 JWT 가 잘못 전달된 경우에도 서명 키가 다르기 때문에
     * 여기서 TOKEN_INVALID 로 자연스럽게 차단됨.
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Client 토큰 만료: {}", e.getMessage());
            throw new JwtAuthException(JwtErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            log.warn("Client 토큰 검증 실패: {}", e.getMessage());
            throw new JwtAuthException(JwtErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 쿠키 만료 시간 (초 단위) — CookieUtil.addCookie(maxAge) 에 그대로 사용
     */
    public int getCookieMaxAge() {
        return (int) (clientProperties.getTokenExpiration() / 1000);
    }

    // ─────────────────────────────────────────────────────────
    // Claims 접근 헬퍼
    // ─────────────────────────────────────────────────────────

    /** Claims 에서 사원 PK 추출 */
    public Long extractEmpId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }
}
