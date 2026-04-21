package com.woongjin.survey.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 에러 페이지 컨트롤러
 * - ClientTokenFilter 등에서 리다이렉트되는 에러 뷰를 처리
 */
@Controller
@RequestMapping("/error")
public class ErrorPageController {

    /**
     * 유효하지 않은 설문 토큰 에러 페이지
     * - ClientTokenFilter: svy_client_token 없음/만료/위변조 시 리다이렉트
     * - SurveyController.intro(): Redis 토큰 유효하지 않을 시 리다이렉트
     */
    @GetMapping("/invalid-token")
    public String invalidToken() {
        return "error/invalid-token";
    }
}
