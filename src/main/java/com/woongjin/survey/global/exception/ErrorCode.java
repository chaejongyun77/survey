package com.woongjin.survey.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 에러 코드
 * - BusinessException과 함께 사용
 * - HTTP 상태코드 + 메시지를 한 곳에서 관리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 설문 ──────────────────────────────────────────
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 설문입니다."),
    SURVEY_DISABLED(HttpStatus.FORBIDDEN, "설문에 참여할 수 없습니다."),
    SURVEY_PERIOD_ENDED(HttpStatus.FORBIDDEN, "설문 기간이 종료되었습니다."),
    SURVEY_NOT_TARGET(HttpStatus.FORBIDDEN, "설문 대상자가 아닙니다."),
    SURVEY_ALREADY_DONE(HttpStatus.CONFLICT, "이미 참여한 설문입니다."),
    SURVEY_HAS_TEMP_SAVE(HttpStatus.OK, "임시저장된 설문이 있습니다.");

    private final HttpStatus status;
    private final String message;
}
