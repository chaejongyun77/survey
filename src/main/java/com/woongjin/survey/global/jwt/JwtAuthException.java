package com.woongjin.survey.global.jwt;

import lombok.Getter;

/**
 * JWT 인증/검증 실패 예외
 * - 필터에서 catch → ApiResponse 직렬화 후 응답
 * - 서비스에서 throw → GlobalExceptionHandler 처리
 */
@Getter
public class JwtAuthException extends RuntimeException {

    private final JwtErrorCode errorCode;

    public JwtAuthException(JwtErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
