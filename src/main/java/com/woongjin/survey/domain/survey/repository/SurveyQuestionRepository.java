package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    /**
     * 설문 ID로 미삭제 문항 목록 조회 (정렬 순서 오름차순)
     * @EntityGraph — items LEFT JOIN FETCH, N+1 방지
     */
    @EntityGraph(attributePaths = {"items"})
    List<SurveyQuestion> findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(Long surveyId);
}
