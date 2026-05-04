package com.woongjin.survey.domain.statistics.domain.statdata;

import java.util.Map;

/**
 * 단일/복수 선택형 집계 결과.
 *
 * [의미]
 *  itemCounts: 선택지 ID 별로 몇 명이 선택했는가
 *
 * [예시]
 *  { "itemCounts": { "1": 541, "2": 192 } }
 *
 * [주의]
 *  - MULTIPLE_CHOICE 의 경우 한 응답자가 여러 선택지를 고를 수 있어
 *    값들의 합 ≠ 응답자 수. 응답자 수는 QuestionStat.totalResponseCount 를 참조.
 */
public record ChoiceStatData(
        Map<Long, Integer> itemCounts
) implements QuestionStatData {
}
