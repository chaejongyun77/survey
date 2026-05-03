package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<Answer, Long> {

    /**
     * 중복 응답 여부 확인
     * - SVY_ID + EMP_ID unique 제약 조건과 대응
     */
    boolean existsBySurveyIdAndEmpId(Long surveyId, Long empId);

    /**
     * 특정 설문의 모든 응답 조회 — 통계 배치 집계용
     * - JSON 컬럼(QST_ANSWR)을 포함해 모두 메모리에 로드
     * - 한 설문 단위 호출이라 1만 건 미만의 응답에서는 부담 없음
     */
    List<Answer> findBySurveyId(Long surveyId);
}
