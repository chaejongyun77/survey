package com.woongjin.survey.global.exception;

import com.woongjin.survey.domain.auth.controller.AuthMessages;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 *
 * [예외 분류]
 * - 비즈니스 예외 (BusinessException): HTTP 200 + success:false
 * - 시스템/인프라 예외: HTTP 4xx/5xx
 *
 * [JwtAuthException 처리 범위]
 * - 필터(JwtAuthenticationFilter)에서 발생: 여기까지 도달하지 않음 → 필터에서 직접 처리
 * - 서비스(AuthService 등)에서 발생: 여기서 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 → HTTP 200 + success:false
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("BusinessException [{}]: {}", e.getErrorCode().name(), e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    /**
     * 데이터 유효성 검사 예외 (400)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("ValidationException: {}", message);
        return ApiResponse.error(message);
    }

    /**
     * 인증 정보 불일치 예외 (401)
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleLoginFailure(AuthenticationException e) {
        log.warn("로그인 정보 불일치: {}", e.getMessage());
        return ApiResponse.error(AuthMessages.INVALID_LOGIN);
    }

    /**
     * JWT 인증 예외 (동적 상태 코드)
     */
    @ExceptionHandler(JwtAuthException.class)
    public ApiResponse<Void> handleJwtAuthException(JwtAuthException e, HttpServletResponse response) {
        log.warn("JwtAuthException [{}]: {}", e.getErrorCode().name(), e.getMessage());
        response.setStatus(e.getErrorCode().getStatus().value());
        return ApiResponse.error(e.getMessage());
    }

    /**
     * DB 무결성 위반 (unique 제약, NOT NULL 등)
     * - 운영 중엔 거의 발생하지 않는 이상 상황 — 500 으로 처리
     * - (SVY_ID, EMP_ID) unique 중복은 사전 checkParticipate 에서 이미 차단됨
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMostSpecificCause().getMessage(), e);
        return ApiResponse.error("데이터 처리 중 오류가 발생했습니다.");
    }

    /**
     * 그 외 모든 예외 (500)
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("UnhandledException: ", e);
        return ApiResponse.error("서버 오류가 발생했습니다.");
    }
}
