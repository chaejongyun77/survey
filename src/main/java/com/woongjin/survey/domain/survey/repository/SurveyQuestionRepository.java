package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    /**
     * 설문 ID로 미삭제 문항 목록 조회 (정렬 순서 오름차순)
     * items(옵션) fetch join — N+1 방지
     */
    @Query("""
            SELECT DISTINCT q
            FROM SurveyQuestion q
            LEFT JOIN FETCH q.items i
            WHERE q.surveyId = :surveyId
              AND q.deletedAt IS NULL
            ORDER BY q.sortOrder ASC
            """)
    List<SurveyQuestion> findBySurveyIdWithItems(@Param("surveyId") Long surveyId);
}
