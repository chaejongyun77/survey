package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.survey.domain.Survey;
import com.woongjin.survey.domain.survey.domain.SurveyParticipateStatus;
import com.woongjin.survey.domain.survey.domain.SurveyTargetPersonId;
import com.woongjin.survey.domain.survey.dto.QuestionDto;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
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
import java.util.List;
import java.util.Optional;

/**
 * 설문 조회 서비스
 * - 설문 인트로 데이터 조회
 * - 설문 참여 가능 여부 검증
 * - 사원 기준 진행중 설문 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyQueryService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyTargetPersonRepository surveyTargetPersonRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmployeeRepository employeeRepository;

    private static final String TEMP_SAVE_KEY_PREFIX = "survey:temp:";

    // =============================================
    // 문항 조회
    // =============================================

    /**
     * 설문 문항 + 옵션 목록 조회
     * GET /api/surveys/{surveyId}/questions
     */
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(Long surveyId) {
        // 설문 존재 여부 검증
        if (!surveyRepository.existsById(surveyId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }

        return surveyQuestionRepository.findBySurveyIdWithItems(surveyId)
                .stream()
                .map(QuestionDto::from)
                .toList();
    }

    // =============================================
    // 인트로 조회
    // =============================================

    /**
     * 설문 인트로 화면 데이터 조회
     * GET /api/surveys/{surveyId}/intro
     */
    @Transactional(readOnly = true)
    public SurveyIntroResponse getIntro(Long surveyId) {
        return surveyRepository.findIntroById(surveyId)
                .orElseThrow(() -> {
                    log.warn("설문을 찾을 수 없음: surveyId={}", surveyId);
                    return new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
                });
    }

    // =============================================
    // 진행중 설문 조회 (사원 기준)
    // =============================================

    /**
     * 사원 ID 기준 진행중 설문 조회
     * - 8081 설문 체크 흐름에서 사용
     */
    @Transactional(readOnly = true)
    public Optional<SurveyIntroResponse> findActiveSurveyByEmpId(Long empId) {
        return surveyRepository.findActiveByEmpId(empId);
    }

    // =============================================
    // 참여 가능 여부 검증
    // =============================================

    /**
     * 설문 참여 가능 여부 검증
     *
     * 검증 순서:
     * 1. 설문 존재 여부     → SURVEY_NOT_FOUND (404)
     * 2. USE_YN = 0        → SURVEY_DISABLED (403)
     * 3. 설문 기간 외       → SURVEY_PERIOD_ENDED (403)
     * 4. 대상자 아님        → SURVEY_NOT_TARGET (403)
     * 5. 임시저장 존재      → HAS_TEMP_SAVE (200)
     * 6. 이미 응답 완료     → SURVEY_ALREADY_DONE (409)
     * 7. 모두 통과          → AVAILABLE
     */
    @Transactional(readOnly = true)
    public SurveyParticipateStatus checkParticipate(Long surveyId, String empNo) {

        // empNo → empId 변환
        Long empId = employeeRepository.findByEmpNo(empNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_TARGET))
                .getId();

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

        // TODO. 임시 저장 여부 체크
       /* String tempKey = TEMP_SAVE_KEY_PREFIX + surveyId + ":" + empId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(tempKey))) {
            return SurveyParticipateStatus.HAS_TEMP_SAVE;
        }*/

        // 6. 중복 응답 여부
        if (surveyResponseRepository.existsBySurveyIdAndEmpId(surveyId, empId)) {
            throw new BusinessException(ErrorCode.SURVEY_ALREADY_DONE);
        }

        return SurveyParticipateStatus.AVAILABLE;
    }
}
