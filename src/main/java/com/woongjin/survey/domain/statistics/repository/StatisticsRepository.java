package com.woongjin.survey.domain.statistics.repository;

import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;
import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;

import java.util.List;
import java.util.Optional;

/**
 * 설문 통계 전용 Repository
 *
 * [역할]
 * - 통계 페이지에서 필요한 집계 쿼리만 담당
 * - 도메인 Repository(SurveyRepository)와 분리하여 책임 명확화
 */
public interface StatisticsRepository {

    /**
     * 설문 기본정보 요약 조회
     * - 설문 메타 + 문항/대상자/응답 카운트를 한 번에 조회
     */
    Optional<SurveySummaryProjection> findSummaryBySurveyId(Long surveyId);

    /**
     * 조직(부서)별 응답률 집계
     * - 대상자가 1명 이상인 부서만 반환
     * - 응답률 내림차순 정렬은 Service 레이어에서 수행
     */
    List<DeptResponseRateProjection> findDeptResponseRates(Long surveyId);
}
