package com.woongjin.survey.domain.statistics.controller;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.service.StatisticsQueryService;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 설문 통계 페이지 — REST API
 *
 * [경로 규약]
 * - 사내 직원(관리자/팀장 등) 전용 → /api/internal 하위로 배치
 * - 통계는 설문 단위로 묶이므로 /surveys/{surveyId}/statistics/* 형태
 */
@RestController
@RequestMapping("/api/internal/v1/surveys/{surveyId}/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsQueryService statisticsQueryService;

    /**
     * 설문 기본정보 요약 조회 — 통계 페이지 상단 영역
     */
    @GetMapping("/summary")
    public ApiResponse<StatisticsSummaryResponse> getSummary(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "설문 기본정보 요약 조회 성공",
                statisticsQueryService.getSummary(surveyId)
        );
    }

    /**
     * 조직(부서)별 응답률 조회 — "응답 결과 조회" 탭 하단 영역
     */
    @GetMapping("/depts")
    public ApiResponse<List<DeptResponseRateResponse>> getDeptResponseRates(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "조직별 응답률 조회 성공",
                statisticsQueryService.getDeptResponseRates(surveyId)
        );
    }
}
