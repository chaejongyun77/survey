package com.woongjin.survey.domain.survey.dto;

import com.woongjin.survey.domain.survey.domain.SurveyQuestionItem;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 문항 항목(옵션) 응답 DTO
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestionItemDto {

    /** 항목 ID */
    private final Long itemId;

    /** 항목 명 */
    private final String itemName;

    /** 정렬 순서 */
    private final Integer sortOrder;

    public static QuestionItemDto from(SurveyQuestionItem item) {
        return QuestionItemDto.builder()
                .itemId(item.getId())
                .itemName(item.getItemName())
                .sortOrder(item.getSortOrder())
                .build();
    }
}
