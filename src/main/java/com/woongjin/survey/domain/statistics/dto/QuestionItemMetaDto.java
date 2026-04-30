package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.survey.domain.QuestionItem;

/**
 * 문항 항목(선택지) 메타 정보 — 통계 응답용
 *
 * [용도]
 * - 응답자별 문항답변 화면에서 항목 ID → 항목명 변환용 매핑 데이터
 * - 예) 응답 JSON 의 selectedItemIds:[2] → 화면에 "macOS" 로 표시
 *
 * [설계 메모]
 * - 삭제된 항목(deletedAt != null)도 포함해야 함
 *   → 과거 응답이 삭제된 항목을 가리키고 있을 수 있기 때문
 *   → 표시 정책(예: "(삭제됨)")은 프론트에서 결정
 */
public record QuestionItemMetaDto(
        Long itemId,
        String itemName,
        Integer sortOrder
) {
    public static QuestionItemMetaDto from(QuestionItem item) {
        return new QuestionItemMetaDto(item.getId(), item.getItemName(), item.getSortOrder());
    }
}
