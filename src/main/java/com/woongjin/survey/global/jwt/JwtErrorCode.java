package com.woongjin.survey.global.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * JWT 관련 에러 코드
 * - 필터 / 서비스 양쪽에서 공통으로 사용
 */
@Getter
@RequiredArgsConstructor
public enum JwtErrorCode {

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요.");

    private final HttpStatus status;
    private final String message;
}
