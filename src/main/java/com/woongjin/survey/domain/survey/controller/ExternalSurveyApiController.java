package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.service.SurveyCreateService;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 외부 시스템(8081)을 위한 설문 API
 * - API Key 검증은 InternalApiKeyFilter 에서 처리 (X-Internal-Api-Key 헤더)
 */
@Slf4j
@RestController
@RequestMapping("/api/external/v1/admin/auth")
@RequiredArgsConstructor
public class ExternalSurveyApiController {

    private final SurveyCreateService surveyCreateService;

    @GetMapping("/survey-check")
    public ApiResponse<String> checkSurvey(@RequestParam String empNo) {
        log.info("외부 설문 체크 요청: empNo={}", empNo);
        return surveyCreateService.issue(empNo);
    }
}
