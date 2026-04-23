package com.woongjin.survey.domain.survey.dto.submit;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 설문 임시저장 API 요청 바디
 *
 * POST /api/external/v1/thinkbig/surveys/{surveyId}/draft
 *
 * SubmitRequest 와 달리 answers 가 비어있어도 허용 (부분 저장 지원)
 */
@Getter
@NoArgsConstructor
public class DraftRequest {

    @Valid
    private List<AnswerDto> answers;

    public List<AnswerDto> getAnswers() {
        return answers != null ? answers : Collections.emptyList();
    }
}
