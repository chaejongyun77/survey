package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.domain.survey.infra.SurveyTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * 설문 View 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyTokenRepository surveyTokenRepository;

    @GetMapping
    public String list(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("empName", principal.getEmpName());
        return "survey/list";
    }

    /**
     * 설문 인트로 페이지
     *
     * 1) URL의 token 파라미터로 Redis 조회 (empNo:surveyId 복원)
     * 2) 유효하면 토큰 즉시 폐기 (단발성)
     * 3) surveyId, empNo를 모델에 담아 intro.html 렌더링
     * 4) 토큰이 없거나 만료됐으면 400 에러 페이지로 이동
     *
     * Security: /surveys/intro 는 인증 없이 접근 가능하도록 설정 필요
     */
    @GetMapping("/intro")
    public String intro(@RequestParam String token, Model model) {

        Optional<String> payloadOpt = surveyTokenRepository.find(token);

        if (payloadOpt.isEmpty()) {
            log.warn("설문 인트로 접근 거부: 유효하지 않거나 만료된 토큰 token={}", token);
            return "error/invalid-token";
        }

        // 단발성 보장 — 조회 즉시 폐기
        surveyTokenRepository.delete(token);

        // payload = "empNo:surveyId"
        String[] parts = payloadOpt.get().split(":");
        String empNo    = parts[0];
        Long   surveyId = Long.parseLong(parts[1]);

        log.info("설문 인트로 접근 허용: empNo={}, surveyId={}", empNo, surveyId);

        model.addAttribute("surveyId", surveyId);
        model.addAttribute("empNo", empNo);

        return "survey/intro";
    }

    /**
     * 설문 응답(참여) 페이지
     *
     * - 인증된 사용자가 직접 접근하거나
     * - test-iframe.html에서 START_SURVEY postMessage 수신 후 이동
     */
    @GetMapping("/response")
    public String response(@RequestParam Long svyId, Model model) {
        model.addAttribute("svyId", svyId);
        return "survey/response";
    }
}
