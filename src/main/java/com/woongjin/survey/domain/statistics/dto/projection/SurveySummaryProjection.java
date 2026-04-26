package com.woongjin.survey.domain.statistics.dto.projection;

import java.time.LocalDateTime;

/**
 * 설문 기본정보 요약 — Repository 조회 결과 Projection
 *
 * [역할]
 * QueryDSL 한 번의 쿼리로 화면에 필요한 카운트 값을 모두 가져온다.
 * 응답률·남은일수 등 가공 값은 포함하지 않으며, Service 레이어에서 조립한다.
 */
public record SurveySummaryProjection(
        Long surveyId,
        String title,
        String site,
        LocalDateTime beginDate,
        LocalDateTime endDate,
        long totalQuestionCount,
        long totalTargetCount,
        long respondedCount
) {
}
