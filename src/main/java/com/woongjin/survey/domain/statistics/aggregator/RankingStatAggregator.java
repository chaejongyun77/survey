package com.woongjin.survey.domain.statistics.aggregator;

import com.woongjin.survey.domain.statistics.domain.statdata.QuestionStatData;
import com.woongjin.survey.domain.statistics.domain.statdata.RankingStatData;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 순위형(RANKING) 응답 집계.
 *
 * [로직]
 * - rankedItemIds 의 배열 순서가 곧 순위 (index 0 = 1순위)
 * - itemId 별로 "어느 순위로 몇 번 뽑혔는지" 이중 맵으로 카운트
 *
 * [출력 예시]
 *  rankCounts = {
 *      16L → {1→2, 2→1},     // itemId 16 이 1순위 2번, 2순위 1번
 *      17L → {1→1, 2→1, 3→1}
 *  }
 */
@Component
public class RankingStatAggregator implements QuestionStatAggregator {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.RANKING;
    }

    @Override
    public QuestionStatData aggregate(List<SurveyAnswerDto> answers) {
        Map<Long, Map<Integer, Integer>> rankCounts = new HashMap<>();

        for (SurveyAnswerDto a : answers) {
            List<Long> ranked = a.getRankedItemIds();
            if (ranked == null) continue;

            for (int i = 0; i < ranked.size(); i++) {
                Long itemId = ranked.get(i);
                int rank = i + 1;                    // 0번 인덱스 = 1순위

                rankCounts
                        .computeIfAbsent(itemId, k -> new HashMap<>())
                        .merge(rank, 1, Integer::sum);
            }
        }
        return new RankingStatData(rankCounts);
    }
}
