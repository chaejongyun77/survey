package com.woongjin.survey.domain.survey.dto;

import com.woongjin.survey.domain.survey.domain.SurveyQuestion;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import lombok.Getter;

import java.util.List;

/**
 * 설문 문항 응답 DTO
 *
 * isBranch  : 조건분기 대상 문항 여부 (parentItemId != null)
 * parentItemId : 이 문항을 활성화시키는 부모 항목 ID
 * childQuestionId : 이 문항 선택 시 활성화될 자식 문항 ID
 */
@Getter
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

    /** 조건분기 대상 문항 여부 (parentItemId != null 이면 true) */
    private final Boolean isBranch;

    /**
     * 조건분기 — 이 문항을 활성화시키는 부모 항목 ID
     * null 이면 분기 없음
     */
    private final Long parentItemId;

    /**
     * 조건분기 — 이 문항 선택 시 활성화될 자식 문항 ID
     * null 이면 분기 없음
     */
    private final Long childQuestionId;

    /** 옵션 목록 (TEXT 유형은 빈 리스트) */
    private final List<QuestionItemDto> options;

    private QuestionDto(
            Long qstId,
            QuestionType qstTyCd,
            String qstCn,
            Boolean esntlYn,
            Integer sortOrder,
            Boolean isBranch,
            Long parentItemId,
            Long childQuestionId,
            List<QuestionItemDto> options
    ) {
        this.qstId          = qstId;
        this.qstTyCd        = qstTyCd;
        this.qstCn          = qstCn;
        this.esntlYn        = esntlYn;
        this.sortOrder      = sortOrder;
        this.isBranch       = isBranch;
        this.parentItemId   = parentItemId;
        this.childQuestionId = childQuestionId;
        this.options        = options;
    }

    public static QuestionDto from(SurveyQuestion question) {
        List<QuestionItemDto> options = question.getItems().stream()
                .filter(item -> item.getDeletedAt() == null)   // 미삭제 항목만
                .map(QuestionItemDto::from)
                .toList();

        return new QuestionDto(
                question.getId(),
                question.getQuestionType(),
                question.getQuestionName(),
                question.getRequired(),
                question.getSortOrder(),
                question.getParentItemId() != null,            // parentItemId 있으면 분기 문항
                question.getParentItemId(),
                question.getChildQuestionId(),
                options
        );
    }
}
