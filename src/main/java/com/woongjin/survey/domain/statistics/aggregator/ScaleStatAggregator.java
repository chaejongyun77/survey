package com.woongjin.survey.domain.statistics.aggregator;

import com.woongjin.survey.domain.statistics.domain.statdata.QuestionStatData;
import com.woongjin.survey.domain.statistics.domain.statdata.ScaleStatData;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 척도형(SCALE) 응답 집계.
 *
 * [로직]
 * - 점수 값(scaleValue) 별로 카운트
 * - 평균을 미리 계산해두어 화면이 매번 계산하지 않도록 함
 *
 * [출력 예시]
 *  ScaleStatData{ valueCounts = {4→1, 5→2}, average = 4.67 }
 */
@Component
public class ScaleStatAggregator implements QuestionStatAggregator {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.SCALE;
    }

    @Override
    public QuestionStatData aggregate(List<SurveyAnswerDto> answers) {
        Map<Integer, Integer> valueCounts = new HashMap<>();
        long sum = 0;
        int validCount = 0;

        for (SurveyAnswerDto a : answers) {
            Integer value = a.getScaleValue();
            if (value == null) continue;

            valueCounts.merge(value, 1, Integer::sum);
            sum += value;
            validCount++;
        }

        double average = (validCount == 0) ? 0.0
                : Math.round((double) sum / validCount * 100) / 100.0;   // 소수 둘째자리

        return new ScaleStatData(valueCounts, average);
    }
}
