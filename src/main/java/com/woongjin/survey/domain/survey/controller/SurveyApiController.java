package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.domain.SurveyParticipateStatus;
import com.woongjin.survey.domain.survey.dto.QuestionDto;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.dto.submit.SubmitRequest;
import com.woongjin.survey.domain.survey.service.SurveyQueryService;
import com.woongjin.survey.domain.survey.service.SurveySubmitService;
import com.woongjin.survey.global.interceptor.ClientInterceptor;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 설문 REST API 컨트롤러
 * - 인증: ClientInterceptor 에서 svy_client_token 검증 후 empNo를 request attribute 로 주입
 */
@Slf4j
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyApiController {

    private final SurveyQueryService surveyQueryService;
    private final SurveySubmitService surveySubmitService;

    @GetMapping("/{surveyId}/intro")
    public ApiResponse<SurveyIntroResponse> getIntro(@PathVariable Long surveyId) {
        return ApiResponse.success("설문 인트로 조회 성공", surveyQueryService.getIntro(surveyId));
    }

    @GetMapping("/{surveyId}/questions")
    public ApiResponse<List<QuestionDto>> getQuestions(@PathVariable Long surveyId) {
        return ApiResponse.success("문항 조회 성공", surveyQueryService.getQuestions(surveyId));
    }

    /**
     * 설문 참여 가능 여부 검증
     * - empId: ClientInterceptor 가 주입한 request attribute 에서 추출
     * - surveyId: @PathVariable 로 전달
     */
    @PostMapping("/{surveyId}/participate")
    public ApiResponse<Void> participate(
            @PathVariable Long surveyId,
            HttpServletRequest request) {

        Long empId = (Long) request.getAttribute(ClientInterceptor.ATTR_EMP_ID);
        log.debug("설문 참여 검증: surveyId={}, empId={}", surveyId, empId);

        SurveyParticipateStatus status =
                surveyQueryService.checkParticipate(surveyId, empId);

        return switch (status) {
            case AVAILABLE     -> ApiResponse.success("참여 가능합니다.");
            case HAS_TEMP_SAVE -> ApiResponse.success("임시저장된 설문이 있습니다.");
            default            -> ApiResponse.success("참여 가능합니다.");
        };
    }

    /**
     * 설문 최종 제출
     * - empId: ClientInterceptor 가 주입한 request attribute 에서 추출
     * - surveyId: @PathVariable 로 전달
     * - body: SubmitRequest (답변 목록)
     *
     * [검증 계층]
     *  1) @Valid : 형식 검증 (필드 null, 길이 등 — AnswerDto / SubmitRequest)
     *  2) Service: 참여 가능 여부 재검증 (checkParticipate)
     *  3) Service: 답변 비즈니스 검증 (SurveyAnswerValidator)
     */
    @PostMapping("/{surveyId}/submit")
    public ApiResponse<Void> submit(
            @PathVariable Long surveyId,
            @Valid @RequestBody SubmitRequest request,
            HttpServletRequest servletRequest) {

        Long empId = (Long) servletRequest.getAttribute(ClientInterceptor.ATTR_EMP_ID);
        log.info("설문 제출 요청: surveyId={}, empId={}, answerCount={}",
                surveyId, empId, request.getAnswers().size());

        surveySubmitService.submit(surveyId, empId, request);

        return ApiResponse.success("설문이 제출되었습니다.");
    }
}
