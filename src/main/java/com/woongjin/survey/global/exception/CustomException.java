package com.woongjin.survey.global.exception;

/**
 * 비즈니스 로직 예외 클래스
 */
public class CustomException extends RuntimeException {

    public CustomException(String message) {
        super(message);
    }
}
