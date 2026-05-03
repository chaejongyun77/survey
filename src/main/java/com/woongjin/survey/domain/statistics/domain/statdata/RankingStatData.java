package com.woongjin.survey.domain.statistics.domain.statdata;

import java.util.Map;

/**
 * 순위 선택형 집계 결과.
 *
 * [의미]
 *  rankCounts: itemId → (순위 → 그 순위로 뽑힌 횟수)
 *  이중 맵 구조.
 *
 * [예시]
 *  {
 *    "rankCounts": {
 *      "16": { "1": 2, "2": 1 },          // itemId 16 이 1순위 2번, 2순위 1번
 *      "17": { "1": 1, "2": 1, "3": 1 }
 *    }
 *  }
 *
 * [활용]
 *  - "1순위로 가장 많이 뽑힌 항목" → 각 itemId 의 "1" 값 비교
 *  - "특정 항목의 순위 분포" → rankCounts.get(itemId) 통째로 표시
 */
public record RankingStatData(
        Map<Long, Map<Integer, Integer>> rankCounts
) implements QuestionStatData {
}
