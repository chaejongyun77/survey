package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.survey.domain.enums.QuestionType;

import java.util.List;

/**
 * 문항별 응답현황 — 한 문항의 통계 응답 DTO.
 *
 * [화면 매핑]
 *  카드 한 장 = 한 문항
 *  - 문항 1: 전반적인 서비스 만족도는 어떠셨나요?
 *    단일선택 · 응답 873명
 *    [items 막대 또는 카드들]
 *
 * [평탄화 설계]
 *  - 모든 타입(Choice/Scale/Subjective/Ranking)을 동일 형태로 매핑
 *  - 화면은 questionType 만 보고 분기 렌더링
 *  - SUBJECTIVE 는 items 가 비어있고 totalResponseCount 만 의미 있음
 *  - average 는 SCALE 일 때만 값이 들어감 (다른 타입은 null)
 */
public record QuestionStatisticsResponse(
        Long questionId,
        QuestionType questionType,
        String title,
        int totalResponseCount,
        List<QuestionStatItemResponse> items,
        Double average           // SCALE 전용 (그 외 null)
) {
}
