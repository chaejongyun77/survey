package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.domain.Survey;
import com.woongjin.survey.domain.survey.domain.SurveyParticipateStatus;
import com.woongjin.survey.domain.survey.domain.SurveyTargetPersonId;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import com.woongjin.survey.domain.survey.repository.SurveyResponseRepository;
import com.woongjin.survey.domain.survey.repository.SurveyTargetPersonRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 설문 참여하기 비즈니스 로직
 *
 * 검증 순서:
 * 1. 설문 존재 여부     → SURVEY_NOT_FOUND (404)
 * 2. USE_YN = 0        → SURVEY_DISABLED (403)
 * 3. 설문 기간 외       → SURVEY_PERIOD_ENDED (403)
 * 4. 대상자 아님        → SURVEY_NOT_TARGET (403)
 * 5. 임시저장 존재      → SURVEY_HAS_TEMP_SAVE (200) - 구현 예정
 * 6. 이미 응답 완료     → SURVEY_ALREADY_DONE (409)
 * 7. 모두 통과          → AVAILABLE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyParticipateService {

    private final SurveyRepository surveyRepository;
    private final SurveyTargetPersonRepository surveyTargetPersonRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final RedisTemplate<String, String> redisTemplate;

    /** Redis 임시저장 키 prefix - 추후 임시저장 기능 구현 시 동일하게 사용 */
    private static final String TEMP_SAVE_KEY_PREFIX = "survey:temp:";

    @Transactional(readOnly = true)
    public SurveyParticipateStatus check(Long surveyId, Long empId) {

        // 1. 설문 존재 여부
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        // 2. 사용 여부 (USE_YN)
        if (!survey.getUseYn()) {
            throw new BusinessException(ErrorCode.SURVEY_DISABLED);
        }

        // 3. 설문 기간
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(survey.getBeginDate()) || now.isAfter(survey.getEndDate())) {
            throw new BusinessException(ErrorCode.SURVEY_PERIOD_ENDED);
        }

        // 4. 대상자 여부
        SurveyTargetPersonId targetId = new SurveyTargetPersonId(surveyId, empId);
        if (!surveyTargetPersonRepository.existsById(targetId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_TARGET);
        }

        // 5. 임시저장 여부 (틀만 - 추후 임시저장 기능 구현 시 채울 것)
        String tempKey = TEMP_SAVE_KEY_PREFIX + surveyId + ":" + empId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(tempKey))) {
            return SurveyParticipateStatus.HAS_TEMP_SAVE;
        }

        // 6. 중복 응답 여부
        if (surveyResponseRepository.existsBySurveyIdAndEmpId(surveyId, empId)) {
            throw new BusinessException(ErrorCode.SURVEY_ALREADY_DONE);
        }

        return SurveyParticipateStatus.AVAILABLE;
    }
}
