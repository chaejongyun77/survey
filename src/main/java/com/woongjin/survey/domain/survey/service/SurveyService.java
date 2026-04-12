package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 설문 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    /**
     * 설문 인트로 화면 데이터 조회
     *
     * 흐름:
     * 1) surveyId로 설문 기본정보 + 대상자 수 조회 (QueryDSL)
     * 2) 현재 날짜가 BGN_DT ~ END_DT 사이면 진행중, 아니면 종료
     * 3) SurveyIntroResponse로 변환 후 반환
     *
     * @param surveyId 설문 ID
     * @return 설문 인트로 응답 DTO
     */
    @Transactional(readOnly = true)
    public SurveyIntroResponse getIntro(Long surveyId) {
        return surveyRepository.findIntroById(surveyId)
                .map(SurveyIntroResponse::from)
                .orElseThrow(() -> {
                    log.warn("설문을 찾을 수 없음: surveyId={}", surveyId);
                    return new IllegalArgumentException("존재하지 않는 설문입니다. surveyId=" + surveyId);
                });
    }
}
