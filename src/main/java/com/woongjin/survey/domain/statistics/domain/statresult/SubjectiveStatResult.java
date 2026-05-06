package com.woongjin.survey.domain.statistics.domain.statresult;

import java.util.List;

/**
 * 주관식 집계 결과.
 * answeredCount: 비어있지 않은 텍스트 응답 수
 * sampleTexts: 배치 시점 텍스트 최대 30건 (통계 페이지 토글 표시용)
 * 예) { "answeredCount": 521, "sampleTexts": ["좋아요", "보통이에요", ...] }
 */
public record SubjectiveStatResult(
        int answeredCount,
        List<String> sampleTexts
) implements QuestionStatResult {
}
