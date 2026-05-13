package com.woongjin.survey.domain.auth.controller;

import com.woongjin.survey.domain.auth.service.AuthService;
import com.woongjin.survey.domain.auth.service.TokenResponse;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.jwt.JwtAuthenticationFilter;
import com.woongjin.survey.global.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    private static final String ACCESS_TOKEN_COOKIE  = JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE;
    private static final String REFRESH_TOKEN_COOKIE = JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * 페이지 이동 중 토큰 만료/없음 시 서버 사이드 재발급
     * unauthorizedEntryPoint → 여기서 재발급 → 원래 페이지로 리다이렉트
     * 리프레시 토큰도 없으면 로그인 페이지로
     */
    @GetMapping("/refresh-redirect")
    public void refreshRedirect(@RequestParam(defaultValue = "/admin") String redirect,
                                HttpServletRequest request,
                                HttpServletResponse response) throws java.io.IOException {

        String refreshToken = CookieUtil.extract(request, REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            response.sendRedirect("/auth/login");
            return;
        }

        try {
            TokenResponse tokens = authService.reissue(refreshToken);
            CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), jwtProperties.getAccessExpiration());
            CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.getRefreshExpiration());
            response.sendRedirect(redirect);
        } catch (JwtAuthException e) {
            CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
            CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);
            response.sendRedirect("/auth/login");
        }
    }
}
