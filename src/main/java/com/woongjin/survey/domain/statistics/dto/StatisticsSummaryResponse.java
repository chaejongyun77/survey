package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 설문 기본정보 요약 응답 DTO
 *
 * [화면 매핑]
 * - 상단: 설문명 · 기간 · 사이트
 * - 카드: 총 대상자 / 응답 완료 / 응답률 / 미응답
 * - 부가: 마감까지 남은 일수, 총 문항 수
 *
 * [설계 메모]
 * - 응답률·미응답수·남은일수는 클라이언트에서 계산해도 되지만,
 *   여러 화면에서 동일하게 표시되므로 서버에서 계산하여 일관성 보장
 * - 응답률은 소수점 첫째자리까지 (69.9% 형태)
 */
public record StatisticsSummaryResponse(
        Long surveyId,
        String title,           // "2026년 상반기 고객 만족도 조사"
        String site,            // "웅진씽크빅"
        LocalDateTime beginDate,
        LocalDateTime endDate,
        int totalQuestionCount, // 총 문항 수
        int totalTargetCount,   // 총 대상자 수
        int respondedCount,     // 응답 완료 수
        int notRespondedCount,  // 미응답 수
        double responseRate,    // 응답률 (%)
        long daysLeft           // 마감까지 남은 일수
) {
    public static StatisticsSummaryResponse from(SurveySummaryProjection p) {
        int targetCnt    = (int) p.totalTargetCount();
        int respondedCnt = (int) p.respondedCount();
        int notResponded = Math.max(targetCnt - respondedCnt, 0);
        double rate      = targetCnt == 0 ? 0.0 : Math.round((double) respondedCnt / targetCnt * 100 * 10) / 10.0;
        long daysLeft    = Math.max(Duration.between(LocalDateTime.now(), p.endDate()).toDays(), 0);
        return new StatisticsSummaryResponse(
                p.surveyId(), p.title(), p.site(), p.beginDate(), p.endDate(),
                (int) p.totalQuestionCount(), targetCnt, respondedCnt, notResponded, rate, daysLeft);
    }
}
