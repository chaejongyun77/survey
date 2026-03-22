package com.woongjin.survey.domain.survey.controller;

import com.woongjin.survey.domain.survey.dto.SurveyCreateRequestDto;
import com.woongjin.survey.domain.survey.dto.SurveyResponseDto;
import com.woongjin.survey.domain.survey.dto.SurveyUpdateRequestDto;
import com.woongjin.survey.domain.survey.service.SurveyService;
import com.woongjin.survey.global.response.ApiResponse;
import com.woongjin.survey.global.auth.LoginMember;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 설문 컨트롤러
 * - View: Thymeleaf 템플릿 반환 (GET /surveys/*)
 * - REST API: JSON 응답 (POST/PUT/DELETE /api/surveys/*)
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    // ========================
    //  View (Thymeleaf)
    // ========================

    /** 목록 페이지 */
    @GetMapping
    public String listPage(Model model) {
        List<SurveyResponseDto> surveys = surveyService.getSurveyList();
        model.addAttribute("surveys", surveys);
        return "survey/list";
    }

    /** 상세 페이지 */
    @GetMapping("/{surveyId}")
    public String detailPage(@PathVariable Long surveyId, Model model) {
        SurveyResponseDto survey = surveyService.getSurvey(surveyId);
        model.addAttribute("survey", survey);
        return "survey/detail";
    }

    /** 등록 폼 페이지 */
    @GetMapping("/new")
    public String createForm() {
        return "survey/form";
    }

    /** 수정 폼 페이지 */
    @GetMapping("/{surveyId}/edit")
    public String editForm(@PathVariable Long surveyId, Model model) {
        SurveyResponseDto survey = surveyService.getSurvey(surveyId);
        model.addAttribute("survey", survey);
        return "survey/form";
    }

    // ========================
    //  REST API
    // ========================

    /** 설문 생성 */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> create(
            @Valid @RequestBody SurveyCreateRequestDto requestDto,
            @AuthenticationPrincipal LoginMember loginMember) {
        surveyService.createSurvey(requestDto, loginMember.getLoginId());
        return ResponseEntity.ok(ApiResponse.success("설문이 생성되었습니다."));
    }

    /** 설문 수정 */
    @PutMapping("/api/{surveyId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long surveyId,
            @Valid @RequestBody SurveyUpdateRequestDto requestDto) {
        surveyService.updateSurvey(surveyId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("설문이 수정되었습니다."));
    }

    /** 설문 삭제 */
    @DeleteMapping("/api/{surveyId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long surveyId) {
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.success("설문이 삭제되었습니다."));
    }
}
