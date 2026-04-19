package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.service.SurveyCreateService;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 외부 시스템(8081)을 위한 설문 API
 */
@Slf4j
@RestController
@RequestMapping("/api/external/v1/admin/auth")
@RequiredArgsConstructor
public class ExternalSurveyApiController {

    private final SurveyCreateService surveyCreateService;

    @Value("${demo.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/survey-check")
    public ApiResponse<String> checkSurvey(
            @RequestParam String empNo,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            log.warn("유효하지 않은 API Key 요청: empNo={}", empNo);
            return ApiResponse.error("인증 실패: 유효하지 않은 API Key");
        }

        log.info("외부 설문 체크 요청: empNo={}", empNo);
        return surveyCreateService.issue(empNo);
    }
}
