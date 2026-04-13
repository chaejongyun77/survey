package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.domain.SurveyParticipateStatus;
import com.woongjin.survey.domain.survey.dto.ParticipateRequest;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.service.SurveyParticipateService;
import com.woongjin.survey.domain.survey.service.SurveyService;
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

    private final SurveyService surveyService;
    private final SurveyParticipateService surveyParticipateService;

    /**
     * 설문 인트로 데이터 조회
     * GET /api/surveys/{surveyId}/intro
     */
    @GetMapping("/{surveyId}/intro")
    public ApiResponse<SurveyIntroResponse> getIntro(@PathVariable Long surveyId) {
        return ApiResponse.success("설문 인트로 조회 성공", surveyService.getIntro(surveyId));
    }

    /**
     * 설문 참여하기
     * POST /api/surveys/{surveyId}/participate
     *
     * 예외(403/404/409)는 BusinessException → GlobalExceptionHandler에서 처리
     * 정상 응답만 여기서 처리:
     *   - AVAILABLE     → 200 참여 가능합니다
     *   - HAS_TEMP_SAVE → 200 임시저장된 설문이 있습니다 (프론트가 팝업 처리)
     */
    @PostMapping("/{surveyId}/participate")
    public ApiResponse<Void> participate(
            @PathVariable Long surveyId,
            @RequestBody @Valid ParticipateRequest request) {

        SurveyParticipateStatus status =
                surveyParticipateService.check(surveyId, request.getEmpId());

        return switch (status) {
            case AVAILABLE     -> ApiResponse.success("참여 가능합니다.");
            case HAS_TEMP_SAVE -> ApiResponse.success("임시저장된 설문이 있습니다.");
            default            -> ApiResponse.success("참여 가능합니다.");
        };
    }
}
