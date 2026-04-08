package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 설문 컨트롤러 (작업 예정)
 */
@Slf4j
@Controller
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyController {

    @GetMapping
    public String list(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("empName", principal.getEmpName());
        return "survey/list";
    }
}
