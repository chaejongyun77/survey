package com.woongjin.survey.domain.statistics.domain.statdata;

import java.util.Map;

/**
 * 척도형(예: 1~5점) 집계 결과.
 *
 * [의미]
 *  valueCounts: 점수 값 별로 몇 명이 응답했는가
 *  average    : 평균 점수 (소수점 둘째자리까지)
 *
 * [예시]
 *  { "valueCounts": { "4": 1, "5": 2 }, "average": 4.67 }
 */
public record ScaleStatData(
        Map<Integer, Integer> valueCounts,
        double average
) implements QuestionStatData {
}
