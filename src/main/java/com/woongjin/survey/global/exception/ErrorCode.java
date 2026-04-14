package com.woongjin.survey.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 에러 코드
 * - BusinessException과 함께 사용
 * - 비즈니스 예외는 GlobalExceptionHandler에서 HTTP 200 + success:false 로 일괄 처리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 설문 ──────────────────────────────────────────
    SURVEY_NOT_FOUND("설문에 참여할 수 없습니다."),
    SURVEY_DISABLED("설문에 참여할 수 없습니다."),
    SURVEY_PERIOD_ENDED("설문 기간이 종료되었습니다."),
    SURVEY_NOT_TARGET("설문 대상자가 아닙니다."),
    SURVEY_ALREADY_DONE("이미 참여한 설문입니다."),
    SURVEY_HAS_TEMP_SAVE("임시저장된 설문이 있습니다.");

    private final String message;
}
