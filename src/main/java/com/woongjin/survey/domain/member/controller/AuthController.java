package com.woongjin.survey.domain.member.controller;

import com.woongjin.survey.domain.member.dto.TokenResponse;
import com.woongjin.survey.domain.member.service.AuthService;
import com.woongjin.survey.global.auth.LoginMember;
import jakarta.servlet.http.Cookie;
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

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "로그아웃 되었습니다.");
        }
        return "auth/login";
    }

    /**
     * 로그인 처리
     *
     * 흐름:
     * 1) AuthService.login()으로 ID/PW 검증 + 토큰 발급
     * 2) Access Token → httpOnly 쿠키에 세팅
     * 3) Refresh Token → httpOnly 쿠키에 세팅
     * 4) /surveys로 리다이렉트
     */
    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {
        try {
            // 1) 로그인 검증 + 토큰 발급
            TokenResponse tokens = authService.login(loginId, password);

            // 2) Access Token 쿠키 세팅
            addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), -1);

            // 3) Refresh Token 쿠키 세팅
            addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), -1);

            // 4) 로그인 성공 → 설문 목록으로 이동
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
     * 2) Access Token, Refresh Token 쿠키 삭제
     * 3) 로그인 페이지로 리다이렉트
     */
    @PostMapping("/logout")
    public String logout(@AuthenticationPrincipal LoginMember loginMember,
                         HttpServletResponse response) {

        if (loginMember != null) {
            // 1) Redis에서 Refresh Token 삭제
            authService.logout(loginMember.getMemberId());
        }

        // 2) 쿠키 삭제 (maxAge=0 → 브라우저가 즉시 삭제)
        addCookie(response, ACCESS_TOKEN_COOKIE, null, 0);
        addCookie(response, REFRESH_TOKEN_COOKIE, null, 0);

        // 3) 로그인 페이지로
        return "redirect:/auth/login?logout=true";
    }

    /**
     * 토큰 재발급
     *
     * 흐름:
     * 1) 쿠키에서 Refresh Token 추출
     * 2) AuthService.reissue()로 새 토큰 발급
     * 3) 새 토큰을 쿠키에 세팅
     * 4) 원래 페이지로 리다이렉트
     */
    @PostMapping("/reissue")
    public String reissue(HttpServletRequest request,
                          HttpServletResponse response) {
        try {
            // 1) 쿠키에서 Refresh Token 추출
            String refreshToken = extractCookie(request, REFRESH_TOKEN_COOKIE);

            if (refreshToken == null) {
                return "redirect:/auth/login";
            }

            // 2) 새 토큰 발급
            TokenResponse tokens = authService.reissue(refreshToken);

            // 3) 새 토큰 쿠키 세팅
            addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), -1);
            addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), -1);

            // 4) 이전 페이지로 (없으면 메인)
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/surveys");

        } catch (Exception e) {
            log.warn("토큰 재발급 실패: {}", e.getMessage());
            // 재발급 실패 → 다시 로그인
            addCookie(response, ACCESS_TOKEN_COOKIE, null, 0);
            addCookie(response, REFRESH_TOKEN_COOKIE, null, 0);
            return "redirect:/auth/login";
        }
    }

    // =============================================
    // 쿠키 유틸리티 메서드
    // =============================================

    /**
     * httpOnly 쿠키 추가
     * @param maxAge -1이면 브라우저 종료 시 삭제 (세션 쿠키), 0이면 즉시 삭제
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // JS에서 접근 불가 (XSS 방어)
        cookie.setPath("/");        // 모든 경로에서 전송
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * 요청에서 특정 이름의 쿠키 값 추출
     */
    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
