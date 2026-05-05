package com.woongjin.survey.domain.statistics.dto;

/**
 * 문항 통계 — 선택지 한 개의 카운트.
 *
 * [용도]
 *  - SINGLE/MULTIPLE  : 선택지별 (label = 선택지 텍스트, count = 선택 횟수)
 *  - SCALE            : 점수별   (label = "5", count = 응답 수)
 *  - RANKING          : 항목별   (label = 항목 텍스트, count = 1순위 카운트)
 *
 * percentage 는 서버에서 미리 계산 (소수 1자리). 분모 0 방어.
 */
public record QuestionStatItemResponse(
        String label,
        int count,
        double percentage
) {
}
