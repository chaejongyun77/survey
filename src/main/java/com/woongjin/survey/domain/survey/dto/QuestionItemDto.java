package com.woongjin.survey.domain.survey.dto;

import com.woongjin.survey.domain.survey.domain.SurveyQuestionItem;
import lombok.Getter;

/**
 * 문항 항목(옵션) 응답 DTO
 */
@Getter
public class QuestionItemDto {

    /** 항목 ID */
    private final Long itemId;

    /** 항목 명 */
    private final String itemName;

    /** 정렬 순서 */
    private final Integer sortOrder;

    private QuestionItemDto(Long itemId, String itemName, Integer sortOrder) {
        this.itemId    = itemId;
        this.itemName  = itemName;
        this.sortOrder = sortOrder;
    }

    public static QuestionItemDto from(SurveyQuestionItem item) {
        return new QuestionItemDto(
                item.getId(),
                item.getItemName(),
                item.getSortOrder()
        );
    }
}
