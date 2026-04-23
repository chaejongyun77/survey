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
 * isBranch        : 조건분기 대상 문항 여부 (parentItemId != null)
 * parentItemId    : 이 문항을 활성화시키는 부모 항목 ID
 * childQuestionId : 이 문항 선택 시 활성화될 자식 문항 ID
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
                .isBranch(question.getParentItemId() != null)   // parentItemId 있으면 분기 문항
                .parentItemId(question.getParentItemId())
                .childQuestionId(question.getChildQuestionId())
                .options(options)
                .build();
    }
}
