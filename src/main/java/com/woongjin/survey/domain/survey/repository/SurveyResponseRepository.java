package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    /**
     * 중복 응답 여부 확인
     * - SVY_ID + EMP_ID unique 제약 조건과 대응
     */
    boolean existsBySurveyIdAndEmpId(Long surveyId, Long empId);
}
