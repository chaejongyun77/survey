package com.woongjin.survey.domain.statistics.aggregator;

import com.woongjin.survey.domain.statistics.domain.statdata.QuestionStatData;
import com.woongjin.survey.domain.statistics.domain.statdata.SubjectiveStatData;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주관식(SUBJECTIVE) 응답 집계.
 *
 * [로직]
 * - 비어있지 않은 텍스트 응답의 수를 카운트
 * - 텍스트 자체는 통계 테이블에 저장하지 않음
 *   (화면에서 필요 시 SVY_RSPN_TB 에서 페이징 조회)
 *
 * [출력 예시]
 *  SubjectiveStatData{ answeredCount = 521 }
 */
@Component
public class SubjectiveStatAggregator implements QuestionStatAggregator {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.SUBJECTIVE;
    }

    @Override
    public QuestionStatData aggregate(List<SurveyAnswerDto> answers) {
        int answered = 0;
        for (SurveyAnswerDto a : answers) {
            String text = a.getTextAnswer();
            if (text != null && !text.isBlank()) {
                answered++;
            }
        }
        return new SubjectiveStatData(answered);
    }
}
