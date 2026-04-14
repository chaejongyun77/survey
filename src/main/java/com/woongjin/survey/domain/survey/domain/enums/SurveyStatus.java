package com.woongjin.survey.domain.survey.domain.enums;

/**
 * 설문 상태 (SVY_STS)
 *
 * DRAFT     : 임시저장 (작성 중)
 * COMPLETED : 작성 완료 (검토 대기)
 * APPROVED  : 승인됨 (게시 가능)
 */
public enum SurveyStatus {
    DRAFT,
    COMPLETED,
    APPROVED
}
