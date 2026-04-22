package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;

import java.util.Optional;

public interface SurveyRepositoryCustom {

    /**
     * 설문 인트로 화면에 필요한 데이터 조회
     * - 설문 기본정보 (제목, 기간)
     * - 대상자 수 (svy_trpsn_tb COUNT)
     *
     * @param surveyId 설문 ID
     */
    Optional<SurveyIntroResponse> findIntroById(Long surveyId);

    /**
     * 특정 사원(empId)이 대상자로 등록된 현재 진행중인 설문 ID 조회
     * - BGN_DT <= now <= END_DT
     * - useYn = true, deletedDate IS NULL, status = APPROVED
     * - svy_trpsn_tb에 empId가 존재
     *
     * @param empId 직원 PK (EMP_ID)
     * @return 진행중인 설문 ID (없으면 empty)
     */
    Optional<Long> findActiveSurveyIdByEmpId(Long empId);
}
