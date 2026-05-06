package com.woongjin.survey.domain.survey.dto;

import com.woongjin.survey.domain.survey.domain.QuestionBranch;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 문항 분기 정의 응답 DTO
 *
 * 부모 문항(parentQstId)에서 특정 항목(parentItemId)이 선택되었을 때
 * 자식 문항(childQstId)이 활성화됨을 표현한다.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BranchDto {

    /** 부모 문항 ID */
    private final Long parentQstId;

    /** 부모 항목 ID — 이 항목 선택 시 자식 문항 활성화 */
    private final Long parentItemId;

    /** 자식 문항 ID — 활성화 대상 */
    private final Long childQstId;

    public static BranchDto from(QuestionBranch branch) {
        return new BranchDto(
                branch.getParentQuestionId(),
                branch.getParentItemId(),
                branch.getChildQuestionId()
        );
    }
}
