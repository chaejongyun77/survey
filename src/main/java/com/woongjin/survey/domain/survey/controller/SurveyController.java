package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.domain.survey.infra.SurveyTokenRepository;
import com.woongjin.survey.global.cookie.CookieUtil;
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
    private final ClientTokenProvider   clientTokenProvider;

    @GetMapping
    public String list(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("empName", principal.getEmpName());
        return "survey/list";
    }

    /**
     * 설문 인트로 페이지
     *
     * [처리 흐름]
     * 1) URL ?token= 으로 Redis 조회 → empNo:surveyId 복원
     * 2) 조회 성공 즉시 Redis 토큰 폐기 (단발성 보장)
     * 3) empNo + surveyId 로 Client JWT 발급
     * 4) Client JWT 를 SameSite=Lax 쿠키에 저장
     *    (HTTP + iframe 환경 — addClientCookie 사용)
     * 5) intro.html 렌더링 (surveyId 만 모델에 포함)
     *
     * [보안]
     * - Redis 토큰은 1회 사용 후 즉시 폐기 → 재사용 불가
     * - Client JWT 는 별도 secretKey 로 서명 → 직원 JWT 와 완전 분리
     * - 이후 /surveys/response 및 /api/surveys/** 는 Client JWT 로만 접근 가능
     */
    @GetMapping("/intro")
    public String intro(@RequestParam String token,
                        HttpServletResponse response,
                        Model model) {

        Optional<String> payloadOpt = surveyTokenRepository.find(token);

        if (payloadOpt.isEmpty()) {
            log.warn("설문 인트로 접근 거부: 유효하지 않거나 만료된 토큰 token={}", token);
            return "error/invalid-token";
        }

        // 단발성 보장 — 조회 즉시 폐기
        surveyTokenRepository.delete(token);

        // payload = "empNo:surveyId"
        String[] parts    = payloadOpt.get().split(":");
        String   empNo    = parts[0];
        Long     surveyId = Long.parseLong(parts[1]);

        // Client JWT 발급 후 쿠키 저장 (empNo 만 담음 — surveyId 는 파라미터로 전달)
        String clientToken = clientTokenProvider.generateClientToken(empNo);
        CookieUtil.addClientCookie(
                response,
                ClientTokenProvider.COOKIE_NAME,
                clientToken,
                clientTokenProvider.getCookieMaxAge()
        );

        log.info("설문 인트로 접근 허용 + Client JWT 발급: empNo={}, surveyId={}", empNo, surveyId);

        // surveyId 만 전달 — empNo 는 이후 요청에서 JWT 클레임으로 추출
        model.addAttribute("surveyId", surveyId);

        return "survey/intro";
    }

    /**
     * 설문 응답(참여) 페이지
     *
     * - svyId 파라미터 제거: surveyId 는 Client JWT 클레임에서 추출
     * - 실제 인증/권한 검증은 ClientInterceptor 에서 처리
     */
    @GetMapping("/response")
    public String response() {
        return "survey/response";
    }
}
