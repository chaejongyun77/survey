package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.service.SurveyCommandService;
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
@RequestMapping("/api/internal/v1/thinkbig/surveys")
@RequiredArgsConstructor
public class SurveyCommandController {

    private final SurveyCommandService surveyCommandService;

    @Value("${demo.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/check")
    public ApiResponse<String> checkSurvey(
            @RequestParam String empNo,
            @RequestHeader("X-Internal-Api-Key") String apiKey) {

        if (!internalApiKey.equals(apiKey)) {
            log.warn("유효하지 않은 API Key 요청: empNo={}", empNo);
            return ApiResponse.error("인증 실패: 유효하지 않은 API Key");
        }

        log.info("외부 설문 체크 요청: empNo={}", empNo);
        return surveyCommandService.issue(empNo);
    }
}
