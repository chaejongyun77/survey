package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.survey.domain.enums.QuestionType;

import java.util.List;

/**
 * 문항 메타 정보 — 통계 응답용
 *
 * [용도]
 * - 응답자별 문항답변 화면의 동적 컬럼 헤더 구성
 *   → 문항 수만큼 컬럼이 생성되므로, 화면 렌더 전 메타 정보가 필수
 * - 응답값(JSON) → 표시 라벨 변환에 사용
 *   → 예) SINGLE_CHOICE 응답 selectedItemIds:[2] 를 "macOS" 로 변환
 *
 * [설계 메모]
 * - QuestionType 별로 화면 표시 방식이 다름
 *   · SINGLE_CHOICE / MULTIPLE_CHOICE / RANKING : items 의 itemName 매핑
 *   · SUBJECTIVE                                : textAnswer 그대로
 *   · SCALE                                     : items 의 itemName 매핑 (scaleValue = itemId)
 * - 표시 정책은 프론트에서 결정 (서버는 raw 메타만 제공)
 */
public record QuestionMetaDto(
        Long questionId,
        String questionName,
        QuestionType type,
        Integer sortOrder,
        List<QuestionItemMetaDto> items   // SUBJECTIVE 는 빈 리스트
) {
}
