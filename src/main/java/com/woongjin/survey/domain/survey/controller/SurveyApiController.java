package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.service.SurveyService;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 설문 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyApiController {

    private final SurveyService surveyService;

    /**
     * 설문 인트로 데이터 조회
     * GET /api/surveys/{surveyId}/intro
     *
     * @param surveyId 설문 ID
     * @return 설문 제목, 기간, 진행상태, 대상자 수
     */
    @GetMapping("/{surveyId}/intro")
    public ApiResponse<SurveyIntroResponse> getIntro(@PathVariable Long surveyId) {
        SurveyIntroResponse response = surveyService.getIntro(surveyId);
        return ApiResponse.success("설문 인트로 조회 성공", response);
    }
}
