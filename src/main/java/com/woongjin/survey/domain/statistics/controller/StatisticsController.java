package com.woongjin.survey.domain.statistics.controller;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionStatisticsListResponse;
import com.woongjin.survey.domain.statistics.dto.ResponseListResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.excel.ExcelUtil;
import com.woongjin.survey.domain.statistics.excel.StatisticsExcelExporter;
import com.woongjin.survey.domain.statistics.excel.history.ExcelDownloadHistService;
import com.woongjin.survey.domain.statistics.service.StatisticsQueryService;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/v1/surveys/{surveyId}/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsQueryService statisticsQueryService;
    private final StatisticsExcelExporter statisticsExcelExporter;
    private final ExcelDownloadHistService excelDownloadHistService;

    @GetMapping("/summary")
    public ApiResponse<StatisticsSummaryResponse> getSummary(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "설문 기본정보 요약 조회 성공",
                statisticsQueryService.getSummary(surveyId)
        );
    }

    @GetMapping("/depts")
    public ApiResponse<List<DeptResponseRateResponse>> getDeptResponseRates(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "조직별 응답률 조회 성공",
                statisticsQueryService.getDeptResponseRates(surveyId)
        );
    }

    @GetMapping("/responses")
    public ApiResponse<ResponseListResponse> getResponseList(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "응답자별 문항답변 조회 성공",
                statisticsQueryService.getResponseList(surveyId)
        );
    }

    @GetMapping("/questions")
    public ApiResponse<QuestionStatisticsListResponse> getQuestionStatistics(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "문항별 응답현황 조회 성공",
                statisticsQueryService.getQuestionStatistics(surveyId)
        );
    }

    /**
     * 통계 엑셀 다운로드
     * - 시트1: 설문 기본정보 요약
     * - 시트2: 조직별 응답현황
     * - 시트3: 응답자별 문항 답변 (전체)
     * - 파일명: 설문통계_{설문명}_{yyyyMMdd-HHmm}.xlsx (RFC 5987 / UTF-8 인코딩)
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStatistics(@PathVariable Long surveyId) {
        StatisticsSummaryResponse summary = statisticsQueryService.getSummary(surveyId);
        List<DeptResponseRateResponse> depts = statisticsQueryService.getDeptResponseRates(surveyId);
        ResponseListResponse respondents = statisticsQueryService.getResponseListForExcel(surveyId);
        QuestionStatisticsListResponse questionStats = statisticsQueryService.getQuestionStatistics(surveyId);
        byte[] body = statisticsExcelExporter.exportAll(summary, depts, respondents, questionStats);
        excelDownloadHistService.save(surveyId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ExcelUtil.attachmentHeader(summary.title()))
                .body(body);
    }
}
