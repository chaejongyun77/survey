package com.woongjin.survey.domain.survey.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 설문 임시 토큰 Redis 저장소
 *
 * Key   : "survey_tk:{token}"
 * Value : "{empNo}:{surveyId}"
 * TTL   : 5분
 */
@Repository
@RequiredArgsConstructor
public class SurveyTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "survey_tk:";
    private static final long TOKEN_TTL_MINUTES = 10L;

    /**
     * 토큰 생성 및 저장
     *
     * @param empNo    사원번호
     * @param surveyId 설문 ID
     * @return 생성된 토큰 (survey_tk_로 시작하는 UUID 기반)
     */
    public String save(String empNo, Long surveyId) {
        String token = "survey_tk_" + UUID.randomUUID().toString().replace("-", "");
        String key   = PREFIX + token;
        String value = empNo + ":" + surveyId;
        redisTemplate.opsForValue().set(key, value, TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 토큰으로 값(empNo:surveyId) 조회
     *
     * @param token 토큰 문자열
     * @return Optional<String> — 없거나 만료됐으면 empty
     */
    public Optional<String> find(String token) {
        String value = redisTemplate.opsForValue().get(PREFIX + token);
        return Optional.ofNullable(value);
    }

    /**
     * 토큰 즉시 폐기 (단발성 토큰 보장)
     *
     * @param token 토큰 문자열
     */
    public void delete(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
