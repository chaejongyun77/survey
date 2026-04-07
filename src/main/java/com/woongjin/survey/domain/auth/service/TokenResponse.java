package com.woongjin.survey.domain.auth.service;

/**
 * 토큰 응답 DTO
 * - AuthService에서 로그인/재발급 결과를 AuthController에 전달
 */
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
