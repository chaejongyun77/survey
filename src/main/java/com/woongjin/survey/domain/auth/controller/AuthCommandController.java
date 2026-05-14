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
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/external/v1/admin/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    private static final String ACCESS_TOKEN_COOKIE  = JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE;
    private static final String REFRESH_TOKEN_COOKIE = JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE;

    @PostMapping("/login")
    public ApiResponse<Void> login(@RequestBody LoginRequest request,
                                   HttpServletResponse response) {

        TokenResponse tokens = authService.login(request.getLoginId(), request.getPassword());
        CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), jwtProperties.getAccessExpiration());
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.getRefreshExpiration());

        return ApiResponse.success("로그인에 성공했습니다.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                    HttpServletResponse response) {

        if (userPrincipal != null) {
            authService.logout(userPrincipal.getEmpId());
        }
        CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
        CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);

        return ApiResponse.success("로그아웃 되었습니다.");
    }

    /**
     * 액세스 토큰 재발급 (api.js interceptor에서 호출)
     * 실패 시 쿠키 삭제 후 예외 → GlobalExceptionHandler가 401 반환
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
            CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
            CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);
            throw e;
        }
    }
}