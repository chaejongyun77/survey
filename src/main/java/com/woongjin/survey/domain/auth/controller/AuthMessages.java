package com.woongjin.survey.domain.auth.controller;

/**
 * Auth 관련 사용자 메시지 상수
 * - 로그인/로그아웃 등 인증 흐름에서 뷰로 전달하는 문자열을 한 곳에서 관리
 */
public final class AuthMessages {

    private AuthMessages() {}

    public static final String INVALID_LOGIN    = "ID 혹은 비밀번호를 잘못 입력하셨거나 등록되지 않은 아이디 입니다.";
    public static final String DISABLED_ACCOUNT = "비활성화된 계정입니다. 문의해주세요.";
    public static final String SYSTEM_ERROR     = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
}
