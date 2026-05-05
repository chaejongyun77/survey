package com.woongjin.survey.domain.statistics.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문항별 응답현황 — 응답 wrapper.
 *
 * [필드]
 *  - aggregatedAt : 마지막 배치 집계 시각. 화면에 신선도 표시용 (배치 데이터 X 면 null)
 *  - questions    : 문항 정렬 순서대로 정렬된 통계 리스트 (배치 안 돌았으면 빈 리스트)
 */
public record QuestionStatisticsListResponse(
        LocalDateTime aggregatedAt,
        List<QuestionStatisticsResponse> questions
) {
}
