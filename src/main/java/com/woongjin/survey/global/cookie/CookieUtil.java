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
     * httpOnly 쿠키 추가
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
