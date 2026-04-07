package com.woongjin.survey.domain.auth.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Redis를 활용한 JWT Refresh Token 저장소
 *
 * - Refresh Token 저장/조회/삭제 (Key: "RT:{memberId}")
 */
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    // =============================================
    // Refresh Token 관리
    // =============================================

    /**
     * Refresh Token 저장
     * - 로그인, 토큰 재발급 시 호출
     * @param memberId  회원 PK
     * @param token     Refresh Token 값
     * @param expirationMs 만료시간 (ms)
     */
    public void saveRefreshToken(Long memberId, String token, long expirationMs) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, token, expirationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh Token 조회
     * - 토큰 재발급 시 저장된 값과 비교 검증용
     * @param memberId 회원 PK
     * @return 저장된 Refresh Token (없으면 null)
     */
    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token 삭제
     * - 로그아웃 시 호출
     * @param memberId 회원 PK
     */
    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.delete(key);
    }

}
