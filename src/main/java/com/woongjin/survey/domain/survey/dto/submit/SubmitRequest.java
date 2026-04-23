package com.woongjin.survey.domain.survey.dto.submit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 설문 제출 API 요청 바디
 *
 * POST /api/external/v1/thinkbig/surveys/{surveyId}/submit
 * {
 *   "answers": [
 *     { "questionId": 101, "type": "SINGLE_CHOICE", "selectedItemIds": [501] },
 *     ...
 *   ]
 * }
 *
 * [검증 범위]
 * - @NotEmpty  : 빈 배열 거부 (최소 1개 답변 필요)
 * - @Valid     : 배열 내부 AnswerDto 의 필드 검증도 수행
 * - empNo / surveyId 는 ClientTokenFilter + @PathVariable 이 담당하므로 여기에 없음
 * - 필수 문항 누락 / 조건분기 정합성 등은 SurveyAnswerValidator 에서 비즈니스 검증
 */
@Getter
@NoArgsConstructor
public class SubmitRequest {

    @NotEmpty(message = "답변이 비어있습니다.")
    @Valid
    private List<SurveyAnswerDto> answers;
}

