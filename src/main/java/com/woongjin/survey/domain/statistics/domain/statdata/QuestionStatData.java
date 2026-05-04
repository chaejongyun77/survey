package com.woongjin.survey.domain.statistics.domain.statdata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 문항 타입별 집계 결과의 공통 타입.
 *
 * [구조]
 * - sealed interface 로 4개 record 만 허용 (타입 안전성)
 * - 각 record 는 한 가지 문항 타입의 집계 형태를 표현
 *
 * [JSON 직렬화/역직렬화]
 * - DB 의 STAT_DATA(JSON) 컬럼에 저장될 때 "type" 필드로 구분
 * - SELECT 시 Jackson 이 "type" 값을 보고 알맞은 record 로 역직렬화
 *
 * 예) ChoiceStatData → {"type":"CHOICE","itemCounts":{...}}
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChoiceStatData.class,     name = "CHOICE"),
        @JsonSubTypes.Type(value = ScaleStatData.class,      name = "SCALE"),
        @JsonSubTypes.Type(value = SubjectiveStatData.class, name = "SUBJECTIVE"),
        @JsonSubTypes.Type(value = RankingStatData.class,    name = "RANKING")
})
public sealed interface QuestionStatData
        permits ChoiceStatData, ScaleStatData, SubjectiveStatData, RankingStatData {
}
