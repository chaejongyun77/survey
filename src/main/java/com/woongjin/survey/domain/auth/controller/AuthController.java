package com.woongjin.survey.domain.auth.controller;

import com.woongjin.survey.domain.auth.service.AuthService;
import com.woongjin.survey.domain.auth.service.TokenResponse;
import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 인증 관련 컨트롤러 (JWT 방식)
 * - 로그인 페이지 렌더링
 * - 로그인 처리 (토큰 발급 → 쿠키 세팅)
 * - 로그아웃 처리 (Redis 삭제 → 쿠키 삭제)
 * - 토큰 재발급
 */
@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    private static final String ACCESS_TOKEN_COOKIE  = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error  != null) model.addAttribute("errorMsg",  "아이디 또는 비밀번호가 올바르지 않습니다.");
        if (logout != null) model.addAttribute("logoutMsg", "로그아웃 되었습니다.");
        return "auth/login";
    }

    /**
     * 로그인 처리
     *
     * 흐름:
     * 1) AuthService.login()으로 ID/PW 검증 + 토큰 발급
     * 2) Access Token, Refresh Token → httpOnly 쿠키에 세팅
     * 3) /surveys 로 리다이렉트
     */
    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {
        try {
            TokenResponse tokens = authService.login(loginId, password);

            CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE,  tokens.accessToken(),  jwtProperties.getAccessExpiration());
            CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.getRefreshExpiration());

            return "redirect:/surveys";

        } catch (Exception e) {
            log.warn("로그인 실패: loginId={}, reason={}", loginId, e.getMessage());
            model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "auth/login";
        }
    }

    /**
     * 로그아웃 처리
     *
     * 흐름:
     * 1) AuthService.logout()으로 Redis에서 Refresh Token 삭제
     * 2) 쿠키 삭제
     * 3) 로그인 페이지로 리다이렉트
     */
    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal UserPrincipal userPrincipal,
                         HttpServletResponse response) {

        if (userPrincipal != null) {
            authService.logout(userPrincipal.getMemberId());
        }

        CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
        CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);

        return "redirect:/auth/login?logout=true";
    }

    /**
     * 토큰 재발급
     *
     * 흐름:
     * 1) 쿠키에서 Refresh Token 추출
     * 2) AuthService.reissue()로 새 토큰 발급
     * 3) 새 토큰을 쿠키에 세팅
     * 4) 이전 페이지로 리다이렉트
     */
    @PostMapping("/reissue")
    public String reissue(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = CookieUtil.extract(request, REFRESH_TOKEN_COOKIE);

            if (refreshToken == null) {
                return "redirect:/auth/login";
            }

            TokenResponse tokens = authService.reissue(refreshToken);

            CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE,  tokens.accessToken(),  jwtProperties.getAccessExpiration());
            CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.getRefreshExpiration());

            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/surveys");

        } catch (Exception e) {
            log.warn("토큰 재발급 실패: {}", e.getMessage());
            CookieUtil.deleteCookie(response, ACCESS_TOKEN_COOKIE);
            CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE);
            return "redirect:/auth/login";
        }
    }
}
