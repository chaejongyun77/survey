package com.woongjin.survey.domain.statistics.domain.statresult;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 문항 타입별 집계 결과의 공통 타입.
 *
 * [JSON 직렬화/역직렬화]
 * - DB 의 STAT_DATA(JSON) 컬럼에 저장될 때 "type" 필드로 구분
 * - SELECT 시 Jackson 이 "type" 값을 보고 알맞은 record 로 역직렬화
 *
 * 예) ChoiceStatResult → {"type":"CHOICE","itemCounts":{...}}
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChoiceStatResult.class,     name = "CHOICE"),
        @JsonSubTypes.Type(value = ScaleStatResult.class,      name = "SCALE"),
        @JsonSubTypes.Type(value = SubjectiveStatResult.class, name = "SUBJECTIVE"),
        @JsonSubTypes.Type(value = RankingStatResult.class,    name = "RANKING")
})
public sealed interface QuestionStatResult
        permits ChoiceStatResult, ScaleStatResult, RankingStatResult, SubjectiveStatResult {
}
