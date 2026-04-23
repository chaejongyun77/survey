package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.dto.QuestionDto;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import com.woongjin.survey.domain.survey.infra.SurveyDraftRepository;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 설문 조회 서비스
 * - 설문 인트로 데이터 조회
 * - 문항 목록 조회
 * - 사원 기준 진행중 설문 조회
 * - 임시저장 조회 (getDraft)
 *
 * [참여 가능 여부 검증]
 * SurveyParticipationValidator 로 분리됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyQueryService {

    private final SurveyRepository         surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyDraftRepository    surveyDraftRepository;

    // =============================================
    // 문항 조회
    // =============================================

    /**
     * 설문 문항 + 옵션 목록 조회
     */
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(Long surveyId) {
        if (!surveyRepository.existsById(surveyId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }

        return surveyQuestionRepository.findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(surveyId)
                .stream()
                .map(QuestionDto::from)
                .toList();
    }

    // =============================================
    // 인트로 조회
    // =============================================

    /**
     * 설문 인트로 화면 데이터 조회
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
     * 사원 ID 기준 진행중 설문 ID 조회
     * - 8081 설문 체크 흐름에서 사용
     */
    @Transactional(readOnly = true)
    public Optional<Long> findActiveSurveyIdByEmpId(Long empId) {
        return surveyRepository.findActiveSurveyIdByEmpId(empId);
    }

    // =============================================
    // 임시저장 조회
    // =============================================

    /**
     * 임시저장 조회
     * - 저장된 draft 없으면 Optional.empty() 반환
     */
    public Optional<List<SurveyAnswerDto>> getDraft(Long surveyId, Long empId) {
        return surveyDraftRepository.find(empId, surveyId);
    }
}

