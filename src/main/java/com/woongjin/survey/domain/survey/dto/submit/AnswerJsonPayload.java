package com.woongjin.survey.domain.survey.dto.submit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SVY_RSPN_TB.QST_ANSWR 컬럼에 저장될 JSON 전체 구조
 *
 * [저장 예시]
 * {
 *   "answers": [
 *     { "questionId": 101, "type": "SINGLE_CHOICE", "selectedItemIds": [501] },
 *     { "questionId": 102, "type": "SUBJECTIVE",    "textAnswer": "..." }
 *   ]
 * }
 *
 * [활용]
 * - 최종 제출 시: 이 객체를 Jackson으로 직렬화 → DB INSERT
 * - 임시저장 시: 동일 구조를 Redis 에 저장 → 최종 제출 시 그대로 재사용
 * - 통계 쿼리: MariaDB JSON_EXTRACT / JSON_CONTAINS 로 직접 조회
 *
 * [제출 일시]
 * - BaseEntity 의 FRST_CRTN_DT 가 제출 일시 역할을 하므로 별도 필드 불필요
 * - 통계 쿼리도 인덱스 활용을 위해 JSON 내부가 아닌 컬럼 기준으로 수행
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerJsonPayload {

    /** 문항별 답변 목록 */
    private List<SurveyAnswerDto> answers;
}
