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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 설문 참여 가능 여부 검증기
 *
 * [역할 분리 의도]
 * SurveyAnswerValidator 가 "제출 답변의 유효성"을 담당하듯,
 * 이 클래스는 "설문 참여 자체의 가능 여부"를 담당한다.
 * SurveyQueryService 에서 검증 로직을 분리하여 순수 조회와 검증 책임을 분리.
 *
 * [사용처]
 * - SurveyCreateService.issue()     : 토큰 발급 전 사전 검증 (이미 응답 완료 여부)
 * - SurveySubmitService.submit()    : 제출 직전 최종 검증 (TOCTOU 방지)
 * - SurveyApiController.participate(): 참여 버튼 클릭 시 검증
 *
 * [검증 순서 — checkParticipate()]
 *  1. 설문 존재 여부
 *  2. USE_YN = false (비활성화)
 *  3. 설문 기간 외
 *  4. 대상자 아님
 *  5. 이미 응답 완료
 *  6. 모두 통과 → AVAILABLE
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyParticipationValidator {

    private final SurveyRepository             surveyRepository;
    private final SurveyTargetPersonRepository surveyTargetPersonRepository;
    private final SurveyResponseRepository     surveyResponseRepository;

    /**
     * 설문 참여 가능 여부 전체 검증
     * - 실패 시 BusinessException throw
     * - 성공 시 SurveyParticipateStatus 반환
     */
    @Transactional(readOnly = true)
    public SurveyParticipateStatus checkParticipate(Long surveyId, Long empId) {

        // 1. 설문 존재 여부
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        // 2. 사용 여부
        if (!survey.getUseYn()) {
            throw new BusinessException(ErrorCode.SURVEY_DISABLED);
        }

        // 3. 설문 기간
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(survey.getBeginDate()) || now.isAfter(survey.getEndDate())) {
            throw new BusinessException(ErrorCode.SURVEY_PERIOD_ENDED);
        }

        // 4. 대상자 여부
        if (!surveyTargetPersonRepository.existsById(new SurveyTargetPersonId(surveyId, empId))) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_TARGET);
        }

        // TODO: 임시저장 여부 체크 (Redis 임시저장 구현 후 활성화)
        // String tempKey = "survey:temp:" + surveyId + ":" + empId;
        // if (Boolean.TRUE.equals(redisTemplate.hasKey(tempKey))) {
        //     return SurveyParticipateStatus.HAS_TEMP_SAVE;
        // }

        // 5. 이미 응답 완료
        if (hasAlreadySubmitted(surveyId, empId)) {
            throw new BusinessException(ErrorCode.SURVEY_ALREADY_DONE);
        }

        return SurveyParticipateStatus.AVAILABLE;
    }

    /**
     * 이미 응답 완료 여부 확인
     *
     * [사용처]
     * - checkParticipate() 내부 (5번 검증)
     * - SurveyCreateService.issue() : 토큰 발급 전 이미 제출 여부 사전 차단
     */
    @Transactional(readOnly = true)
    public boolean hasAlreadySubmitted(Long surveyId, Long empId) {
        return surveyResponseRepository.existsBySurveyIdAndEmpId(surveyId, empId);
    }
}
