package com.woongjin.survey.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외
 * - ErrorCode 메시지를 그대로 사용
 * - GlobalExceptionHandler에서 HTTP 200 + success:false 로 처리
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
