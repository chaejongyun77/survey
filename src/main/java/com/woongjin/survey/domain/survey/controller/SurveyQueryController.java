package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.dto.SurveyQuestionsResponse;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import com.woongjin.survey.domain.survey.dto.submit.SubmitRequest;
import com.woongjin.survey.domain.survey.service.SurveyCommandService;
import com.woongjin.survey.domain.survey.service.SurveyParticipationValidator;
import com.woongjin.survey.domain.survey.service.SurveyQueryService;
import com.woongjin.survey.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 설문 REST API 컨트롤러
 * - 인증: ClientTokenFilter 가 svy_client_token 검증 후 SecurityContext 에 empId(Long) 세팅
 */
@Slf4j
@RestController
@RequestMapping("/api/external/v1/thinkbig/surveys")
@RequiredArgsConstructor
public class SurveyQueryController {

    private final SurveyQueryService           surveyQueryService;
    private final SurveyCommandService         surveyCommandService;
    private final SurveyParticipationValidator participationValidator;

    @GetMapping("/{surveyId}/intro")
    public ApiResponse<SurveyIntroResponse> getIntro(@PathVariable Long surveyId) {
        return ApiResponse.success("설문 인트로 조회 성공", surveyQueryService.getIntro(surveyId));
    }

    @GetMapping("/{surveyId}/questions")
    public ApiResponse<SurveyQuestionsResponse> getQuestions(@PathVariable Long surveyId) {
        return ApiResponse.success("문항 조회 성공", surveyQueryService.getQuestions(surveyId));
    }

    @PostMapping("/{surveyId}/participate")
    public ApiResponse<Void> participate(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal Long empId) {

        log.debug("설문 참여 검증: surveyId={}, empId={}", surveyId, empId);
        return participationValidator.checkParticipate(surveyId, empId);
    }

    /**
     * 임시저장
     * - answers 가 비어있어도 허용 (부분 저장 지원)
     * - Redis 에 7일 TTL 로 저장
     */
    /**
     * 임시저장 — @Valid 없이 받아서 빈 answers 허용
     * (SubmitRequest 의 @NotEmpty 는 @Valid 가 있어야 동작함)
     */
    @PostMapping("/{surveyId}/draft")
    public ApiResponse<Void> saveDraft(
            @PathVariable Long surveyId,
            @RequestBody SubmitRequest request,
            @AuthenticationPrincipal Long empId) {

        log.info("임시저장 요청: surveyId={}, empId={}", surveyId, empId);
        surveyCommandService.saveDraft(surveyId, empId, request);
        return ApiResponse.success("임시저장되었습니다.");
    }

    /**
     * 임시저장 조회
     * - 저장된 draft 없으면 data: null 반환
     */
    @GetMapping("/{surveyId}/draft")
    public ApiResponse<List<SurveyAnswerDto>> getDraft(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal Long empId) {

        return surveyQueryService.getDraft(surveyId, empId)
                .map(answers -> ApiResponse.success("임시저장 조회 성공", answers))
                .orElse(ApiResponse.success("임시저장 없음", null));
    }

    /**
     * 임시저장 삭제
     * - 인트로에서 "새로 시작" 선택 시 호출
     */
    @PostMapping("/{surveyId}/draft/delete")
    public ApiResponse<Void> deleteDraft(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal Long empId) {

        surveyCommandService.deleteDraft(surveyId, empId);
        return ApiResponse.success("임시저장이 삭제되었습니다.");
    }

    /**
     * 설문 최종 제출
     *
     * [검증 계층]
     *  1) @Valid : 형식 검증 (필드 null, 길이 등 — SurveyAnswerDto / SubmitRequest)
     *  2) Service: 참여 가능 여부 재검증 (checkParticipate)
     *  3) Service: 답변 비즈니스 검증 (SurveyAnswerValidator)
     */
    @PostMapping("/{surveyId}/submit")
    public ApiResponse<Void> submit(
            @PathVariable Long surveyId,
            @Valid @RequestBody SubmitRequest request,
            @AuthenticationPrincipal Long empId) {

        log.info("설문 제출 요청: surveyId={}, empId={}, answerCount={}",
                surveyId, empId, request.getAnswers().size());
        surveyCommandService.submit(surveyId, empId, request);

        return ApiResponse.success("설문이 제출되었습니다.");
    }
}
