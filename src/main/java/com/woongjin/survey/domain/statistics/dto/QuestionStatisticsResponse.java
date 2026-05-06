package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.survey.domain.enums.QuestionType;

import java.util.List;

/**
 * 문항별 응답현황 — 한 문항의 통계 응답 DTO.
 * 화면은 questionType 으로 분기 렌더링:
 *  - CHOICE/RANKING : items 막대
 *  - SCALE          : items 카드 + average
 *  - SUBJECTIVE     : sampleTexts 토글 (items 비어있음, average null)
 */
public record QuestionStatisticsResponse(
        Long questionId,
        QuestionType questionType,
        String title,
        int totalResponseCount,
        List<QuestionStatItemResponse> items,
        Double average,          // SCALE 전용 (그 외 null)
        List<String> sampleTexts // SUBJECTIVE 전용 (그 외 null)
) {
}
