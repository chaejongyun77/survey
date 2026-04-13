package com.woongjin.survey.domain.survey.domain;

/**
 * 설문 참여 가능 여부 상태값
 */
public enum SurveyParticipateStatus {

    /** 참여 가능 */
    AVAILABLE,

    /** USE_YN = 0 → 비활성화된 설문 */
    DISABLED,

    /** 설문 기간 외 (시작 전 또는 종료 후) */
    PERIOD_ENDED,

    /** SVY_TRPSN_TB에 대상자로 등록되지 않음 */
    NOT_TARGET,

    /** Redis에 임시저장 데이터 존재 (이어서 작성 여부 확인 필요) */
    HAS_TEMP_SAVE,

    /** 이미 응답 완료 */
    ALREADY_DONE
}
