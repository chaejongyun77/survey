package com.woongjin.survey.domain.survey.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 설문 임시저장 Redis 저장소
 *
 * Key   : "survey:draft:{empId}:{surveyId}"
 * Value : JSON 직렬화된 List<SurveyAnswerDto>
 * TTL   : 7일
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SurveyDraftRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "survey:draft:";
    private static final long DRAFT_TTL_DAYS = 7L;

    public void save(Long empId, Long surveyId, List<SurveyAnswerDto> answers) {
        try {
            String json = objectMapper.writeValueAsString(answers);
            redisTemplate.opsForValue().set(key(empId, surveyId), json, DRAFT_TTL_DAYS, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("임시저장 직렬화 실패: empId={}, surveyId={}", empId, surveyId, e);
            throw new RuntimeException("임시저장 중 오류가 발생했습니다.", e);
        }
    }

    public Optional<List<SurveyAnswerDto>> find(Long empId, Long surveyId) {
        String json = redisTemplate.opsForValue().get(key(empId, surveyId));
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            log.error("임시저장 역직렬화 실패: empId={}, surveyId={}", empId, surveyId, e);
            return Optional.empty();
        }
    }

    public void delete(Long empId, Long surveyId) {
        redisTemplate.delete(key(empId, surveyId));
    }

    private String key(Long empId, Long surveyId) {
        return PREFIX + empId + ":" + surveyId;
    }
}
