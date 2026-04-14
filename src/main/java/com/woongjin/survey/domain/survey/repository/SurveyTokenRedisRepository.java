package com.woongjin.survey.domain.survey.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 설문 임시 토큰 Redis 저장소
 *
 * Key:   SVY_TK:{token}
 * Value: {empId}:{surveyId}
 * TTL:   5분
 */
@Repository
@RequiredArgsConstructor
public class SurveyTokenRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX      = "SVY_TK:";
    private static final long   TTL_MINUTES = 5;

    /**
     * 토큰 저장
     */
    public void save(String token, Long empId, Long surveyId) {
        redisTemplate.opsForValue()
                .set(PREFIX + token, empId + ":" + surveyId, TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 토큰 조회
     * @return "{empId}:{surveyId}" 문자열, 없거나 만료 시 null
     */
    public String get(String token) {
        return redisTemplate.opsForValue().get(PREFIX + token);
    }

    /**
     * 토큰 즉시 삭제 (일회용 폐기)
     */
    public void delete(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
