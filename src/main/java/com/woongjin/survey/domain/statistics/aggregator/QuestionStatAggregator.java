package com.woongjin.survey.domain.statistics.aggregator;

import com.woongjin.survey.domain.statistics.domain.statdata.QuestionStatData;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;

import java.util.List;

/**
 * 문항 타입별 집계 전략.
 *
 * [역할]
 * - "한 문항의 응답 리스트" → "그 문항의 통계 결과(QuestionStatData)" 로 변환
 * - 타입별로 구현체 1개씩 (Choice / Scale / Subjective / Ranking)
 *
 * [확장성]
 * - 새 문항 타입이 추가되면 새 Aggregator 클래스 1개만 추가하면 됨
 * - Spring 이 List<QuestionStatAggregator> 로 모든 구현체를 자동 주입
 * - supports() 로 타입을 매칭해 호출
 *
 * [순수 함수]
 * - DB 의존성 없음 (응답 리스트만 받음)
 * - 단위 테스트 용이
 */
public interface QuestionStatAggregator {

    /** 이 Aggregator 가 처리하는 문항 타입인지 */
    boolean supports(QuestionType type);

    /** 응답 리스트를 받아 통계 데이터로 변환 */
    QuestionStatData aggregate(List<SurveyAnswerDto> answers);
}
