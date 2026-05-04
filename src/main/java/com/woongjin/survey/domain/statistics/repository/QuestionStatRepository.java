package com.woongjin.survey.domain.statistics.repository;

import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 설문 문항별 통계 Repository.
 *
 * [용도]
 * - 배치(Writer)가 UPSERT 시 사용 (deleteBySurveyId + saveAll)
 * - 통계 페이지가 조회 시 사용 (findBySurveyId)
 *
 * [UPSERT 전략]
 * - 한 설문의 통계는 통째로 갱신되므로 "DELETE BY 설문ID 후 INSERT" 가 가장 단순
 * - JpaRepository 의 기본 deleteBy* 는 SELECT 후 1행씩 DELETE 라 N+1 발생
 *   → 직접 벌크 DELETE 쿼리로 작성
 */
public interface QuestionStatRepository extends JpaRepository<QuestionStat, Long> {

    /**
     * 특정 설문의 모든 문항 통계 조회 — 화면 표시용
     */
    List<QuestionStat> findBySurveyId(Long surveyId);

    /**
     * 특정 설문의 모든 문항 통계 일괄 삭제 — 배치 UPSERT 의 첫 단계
     * @return 삭제된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("delete from QuestionStat q where q.surveyId = :surveyId")
    int deleteAllBySurveyId(Long surveyId);
}
