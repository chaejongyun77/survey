package com.woongjin.survey.global.exception;

import com.woongjin.survey.domain.auth.controller.AuthMessages;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 *
 * [JwtAuthException 처리 범위]
 * - 필터(JwtAuthenticationFilter)에서 발생: 여기까지 도달하지 않음 → 필터에서 직접 처리
 * - 서비스(AuthService 등)에서 발생: 여기서 처리
 */
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 데이터 유효성 검사 예외 (400) */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // ApiResponse에 badRequest() 메서드가 있다고 가정 (또는 error(message) 사용 가능)
        return ApiResponse.error(message);
    }

    /** 인증 정보 불일치 예외 (401) - 아이디/비밀번호 틀림 */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({AuthenticationException.class})
    public ApiResponse<Void> handleLoginFailure(Exception e) {
        log.warn("로그인 정보 불일치: {}", e.getMessage());
        return ApiResponse.error(AuthMessages.INVALID_LOGIN);
    }

    /** JWT 인증 예외 (동적 상태 코드) */
    @ExceptionHandler(JwtAuthException.class)
    public ApiResponse<Void> handleJwtAuthException(JwtAuthException e, HttpServletResponse response) {
        log.warn("JwtAuthException [{}]: {}", e.getErrorCode().name(), e.getMessage());
        // Custom ErrorCode의 동적 상태값을 세팅
        response.setStatus(e.getErrorCode().getStatus().value());
        return ApiResponse.error(e.getMessage());
    }

    /** 비즈니스 커스텀 예외 (404 예시) */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CustomException.class)
    public ApiResponse<Void> handleCustomException(CustomException e) {
        log.warn("CustomException: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    /** 그 외 모든 예외 (500) */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("UnhandledException: ", e);
        return ApiResponse.error("서버 오류가 발생했습니다.");
    }
}
