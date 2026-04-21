package com.woongjin.survey.domain.auth.controller;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.domain.auth.service.AuthService;
import com.woongjin.survey.domain.auth.service.TokenResponse;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.jwt.JwtAuthenticationFilter;
import com.woongjin.survey.global.jwt.JwtErrorCode;
import com.woongjin.survey.global.jwt.JwtProperties;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/external/v1/admin/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    private static final String ACCESS_TOKEN_COOKIE = JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE;
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    /**
     * 로그인 처리
     * 정상 로직만 작성합니다. 예외 발생 시 GlobalExceptionHandler가 401/403 에러로 처리합니다.
     */
    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody LoginRequest request,
                                   HttpServletResponse response) {

        TokenResponse tokens = authService.login(request.getLoginId(), request.getPassword());

        CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), jwtProperties.getAccessExpiration());
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.getRefreshExpiration());

        // @ResponseStatus를 안 붙이면 기본값인 200 OK로 전송됩니다.
        return ApiResponse.success("로그인에 성공했습니다.");
    }

    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                    HttpServletResponse response) {

        if (userPrincipal != null) {
            authService.logout(userPrincipal.getEmpId());
        }

        clearCookies(response);

        return ApiResponse.success("로그아웃 되었습니다.");
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/reissue")
    public ApiResponse<Void> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = CookieUtil.extract(request, REFRESH_TOKEN_COOKIE);

        if (refreshToken == null) {
            throw new JwtAuthException(JwtErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        try {
            TokenResponse tokens = authService.reissue(refreshToken);

            CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), jwtProperties.getAccessExpiration());
            CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.getRefreshExpiration());

            return ApiResponse.success("토큰이 재발급 되었습니다.");

        } catch (JwtAuthException e) {
            // 인증 예외 시에만 쿠키를 지우고 예외를 던져 GlobalExceptionHandler로 보냅니다.
            clearCookies(response);
            throw e;
        }
    }

    private void clearCookies(HttpServletResponse response) {
        CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
        CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }
}