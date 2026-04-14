package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.dto.SurveyTokenResponse;
import com.woongjin.survey.domain.survey.service.SurveyCreateService;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 외부 시스템(8081)을 위한 설문 API
 */
@Slf4j
@RestController
@RequestMapping("/api/external/v1/admin/auth")
@RequiredArgsConstructor
public class ExternalSurveyApiController {

    private final SurveyCreateService surveyCreateService;

    @GetMapping("/survey-check")
    public ApiResponse<SurveyTokenResponse> checkSurvey(@RequestParam String empNo) {
        log.info("외부 설문 체크 요청: empNo={}", empNo);
        return ApiResponse.success("설문 체크 완료", surveyCreateService.issue(empNo));
    }
}
