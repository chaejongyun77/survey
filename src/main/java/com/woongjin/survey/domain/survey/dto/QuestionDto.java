package com.woongjin.survey.domain.survey.dto;

import com.woongjin.survey.domain.survey.domain.Question;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 설문 문항 응답 DTO
 *
 * 분기 정보는 별도의 BranchDto / SurveyQuestionsResponse 로 분리되어 전달된다.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionDto {

    /** 문항 ID */
    private final Long qstId;

    /** 문항 유형 */
    private final QuestionType qstTyCd;

    /** 문항 명 */
    private final String qstCn;

    /** 필수 응답 여부 */
    private final Boolean esntlYn;

    /** 정렬 순서 */
    private final Integer sortOrder;

    /** 옵션 목록 (SUBJECTIVE 유형은 빈 리스트) */
    private final List<QuestionItemDto> options;

    public static QuestionDto from(Question question) {
        List<QuestionItemDto> options = question.getItems().stream()
                .filter(item -> item.getDeletedAt() == null)   // 미삭제 항목만
                .map(QuestionItemDto::from)
                .toList();

        return QuestionDto.builder()
                .qstId(question.getId())
                .qstTyCd(question.getQuestionType())
                .qstCn(question.getQuestionName())
                .esntlYn(question.getRequired())
                .sortOrder(question.getSortOrder())
                .options(options)
                .build();
    }
}
