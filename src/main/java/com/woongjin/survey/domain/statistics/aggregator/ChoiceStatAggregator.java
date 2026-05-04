package com.woongjin.survey.domain.statistics.aggregator;

import com.woongjin.survey.domain.statistics.domain.statdata.ChoiceStatData;
import com.woongjin.survey.domain.statistics.domain.statdata.QuestionStatData;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 선택형(SINGLE/MULTIPLE) 응답 집계.
 *
 * [로직]
 * - 각 응답의 selectedItemIds 를 펼쳐 itemId 별로 카운트
 * - MULTIPLE 의 경우 한 응답자가 여러 itemId 를 갖고 있어도 그대로 카운트
 *
 * [출력 예시]
 *  ChoiceStatData{ itemCounts = { 1L → 1, 2L → 2 } }
 */
@Component
public class ChoiceStatAggregator implements QuestionStatAggregator {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE;
    }

    @Override
    public QuestionStatData aggregate(List<SurveyAnswerDto> answers) {
        Map<Long, Integer> itemCounts = new HashMap<>();

        for (SurveyAnswerDto a : answers) {
            List<Long> selected = a.getSelectedItemIds();
            if (selected == null) continue;        // 데이터 정합성 방어

            for (Long itemId : selected) {
                itemCounts.merge(itemId, 1, Integer::sum);
            }
        }
        return new ChoiceStatData(itemCounts);
    }
}
