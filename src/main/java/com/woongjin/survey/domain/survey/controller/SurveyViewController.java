package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResult;
import com.woongjin.survey.domain.survey.service.SurveyCommandService;
import com.woongjin.survey.global.cookie.CookieUtil;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 설문 View 컨트롤러
 * - 뷰 렌더링 전용, 비즈니스 로직은 SurveyCommandService 에 위임
 */
@Slf4j
@Controller
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyViewController {

    private final SurveyCommandService surveyCommandService;
    private final ClientTokenProvider  clientTokenProvider;

    @GetMapping
    public String list(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("empName", principal.getEmpName());
        return "survey/list";
    }

    /**
     * 설문 인트로 페이지
     * - 비즈니스 로직(토큰 검증, empId 변환, Client JWT 발급)은 SurveyCommandService 에 위임
     * - 쿠키 발급은 Controller 책임 (Service 계층에 HttpServletResponse 침투 방지)
     */
    @GetMapping("/client/intro")
    public String intro(@RequestParam String token,
                        HttpServletResponse response,
                        Model model) {
        try {
            SurveyIntroResult ctx = surveyCommandService.processIntroToken(token);

            // 쿠키 심기 — Web 계층 책임이므로 Controller 에서 수행
            CookieUtil.addClientCookie(response,
                    ClientTokenProvider.COOKIE_NAME,
                    ctx.getClientToken(),
                    clientTokenProvider.getCookieMaxAge()
            );

            model.addAttribute("surveyId", ctx.getSurveyId());
            return "survey/intro";

        } catch (BusinessException e) {
            log.warn("설문 인트로 진입 거부: {}", e.getMessage());
            return "error/invalid-token";
        }
    }

    /**
     * 설문 응답(참여) 페이지
     * - 실제 인증/권한 검증은 ClientTokenFilter 에서 처리
     */
    @GetMapping("/client/response")
    public String response() {
        return "survey/response";
    }
}

