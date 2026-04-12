package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.dto.SurveyIntroDto;

import java.util.Optional;

public interface SurveyRepositoryCustom {

    /**
     * 설문 인트로 화면에 필요한 데이터 조회
     * - 설문 기본정보 (제목, 기간, 상태)
     * - 대상자 수 (svy_trpsn_tb COUNT)
     *
     * @param surveyId 설문 ID
     */
    Optional<SurveyIntroDto> findIntroById(Long surveyId);
}
