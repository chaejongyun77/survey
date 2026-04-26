package com.woongjin.survey.domain.statistics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 설문 통계 페이지 — Thymeleaf View 컨트롤러
 *
 * [경로]
 *  GET /surveys/{surveyId}/statistics → statistics/statistics.html
 *
 * [인증]
 *  /surveys/client/** 가 아니므로 employeeFilterChain 이 처리.
 *  → 직원 인증(ACCESS_TOKEN) 필요. 미인증 시 /auth/login 으로 리다이렉트.
 *
 * [데이터 흐름]
 *  - 페이지 자체는 빈 골격만 렌더링하고, 실제 데이터는 페이지 진입 후
 *    Axios 가 /api/internal/v1/surveys/{id}/statistics/* 로 비동기 조회
 */
@Controller
@RequiredArgsConstructor
public class StatisticsViewController {

    @GetMapping("/surveys/{surveyId}/statistics")
    public String page(@PathVariable Long surveyId, Model model) {
        model.addAttribute("surveyId", surveyId);
        return "statistics/statistics";
    }
}
