package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.domain.SurveyParticipateStatus;
import com.woongjin.survey.domain.survey.dto.ParticipateRequest;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.service.SurveyQueryService;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 설문 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyApiController {

    private final SurveyQueryService surveyQueryService;

    @GetMapping("/{surveyId}/intro")
    public ApiResponse<SurveyIntroResponse> getIntro(@PathVariable Long surveyId) {
        return ApiResponse.success("설문 인트로 조회 성공", surveyQueryService.getIntro(surveyId));
    }

    @PostMapping("/{surveyId}/participate")
    public ApiResponse<Void> participate(
            @PathVariable Long surveyId,
            @RequestBody @Valid ParticipateRequest request) {

        SurveyParticipateStatus status =
                surveyQueryService.checkParticipate(surveyId, request.getEmpId());

        return switch (status) {
            case AVAILABLE     -> ApiResponse.success("참여 가능합니다.");
            case HAS_TEMP_SAVE -> ApiResponse.success("임시저장된 설문이 있습니다.");
            default            -> ApiResponse.success("참여 가능합니다.");
        };
    }
}
