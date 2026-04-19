package com.woongjin.survey.global.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 쿠키 유틸리티
 * - 인스턴스화 불필요 → 모든 메서드 static
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtil {

    /**
     * httpOnly 쿠키 추가 (직원 인증용 — 기존 방식)
     *
     * @param maxAge -1 : 브라우저 종료 시 삭제 (세션 쿠키)
     *               0  : 즉시 삭제
     *               양수: 초 단위 유지
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // JS 접근 차단 (XSS 방어)
        cookie.setPath("/");       // 모든 경로에서 전송
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * 설문 참여자용 Client 토큰 쿠키 추가
     *
     * [iframe + HTTP 환경 대응]
     *  - Java의 Cookie API는 SameSite 속성을 직접 지원하지 않음
     *  - Set-Cookie 헤더를 직접 작성하여 SameSite=Lax 명시
     *  - HTTP 환경이므로 Secure 플래그는 제외 (Secure + SameSite=None 은 HTTPS 필요)
     *  - SameSite=Lax : 같은 도메인 내 iframe 이동/폼 전송에서 쿠키 전송 허용
     *
     * @param response HTTP 응답
     * @param name     쿠키 이름
     * @param value    쿠키 값 (JWT 문자열)
     * @param maxAge   만료 시간 (초)
     */
    public static void addClientCookie(HttpServletResponse response, String name, String value, int maxAge) {
        String cookieHeader = name + "=" + value
                + "; Path=/"
                + "; Max-Age=" + maxAge
                + "; HttpOnly"
                + "; SameSite=Lax";
        response.addHeader("Set-Cookie", cookieHeader);
    }

    /**
     * 설문 참여자용 Client 토큰 쿠키 즉시 삭제
     */
    public static void deleteClientCookie(HttpServletResponse response, String name) {
        String cookieHeader = name + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax";
        response.addHeader("Set-Cookie", cookieHeader);
    }

    /**
     * 쿠키 즉시 삭제 (maxAge=0)
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, null, 0);
    }

    /**
     * 요청에서 특정 이름의 쿠키 값 추출
     *
     * @return 쿠키 값, 없으면 null
     */
    public static String extract(HttpServletRequest request, String name) {
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
