package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;

import java.time.Duration;
import java.time.LocalDateTime;

public record StatisticsSummaryResponse(
        Long surveyId,
        String title,
        String site,
        LocalDateTime beginDate,
        LocalDateTime endDate,
        int totalQuestionCount,
        int totalTargetCount,
        int respondedCount,
        int notRespondedCount,
        double responseRate,    // %, 소수 첫째자리
        long daysLeft
) {
    public static StatisticsSummaryResponse from(SurveySummaryProjection p) {
        int targetCnt    = (int) p.totalTargetCount();
        int respondedCnt = (int) p.respondedCount();
        int notResponded = Math.max(targetCnt - respondedCnt, 0);
        double rate      = targetCnt == 0 ? 0.0 : Math.round((double) respondedCnt / targetCnt * 1000) / 10.0;
        long daysLeft    = Math.max(Duration.between(LocalDateTime.now(), p.endDate()).toDays(), 0);

        return new StatisticsSummaryResponse(
                p.surveyId(),
                p.title(),
                p.site(),
                p.beginDate(),
                p.endDate(),
                (int) p.totalQuestionCount(),
                targetCnt,
                respondedCnt,
                notResponded,
                rate,
                daysLeft
        );
    }
}
