package com.woongjin.survey.domain.statistics.controller;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionStatisticsListResponse;
import com.woongjin.survey.domain.statistics.dto.ResponseListResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.excel.StatisticsExcelExporter;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter FILENAME_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final StatisticsQueryService statisticsQueryService;
    private final StatisticsExcelExporter statisticsExcelExporter;

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

    /**
     * 응답자별 문항답변 — 최근 N건 미리보기 조회
     * - 화면 미리보기용 (현재 50건)
     * - 전체 응답은 엑셀 다운로드 API 로 별도 제공
     */
    @GetMapping("/responses")
    public ApiResponse<ResponseListResponse> getResponseList(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "응답자별 문항답변 조회 성공",
                statisticsQueryService.getResponseList(surveyId)
        );
    }

    /**
     * 문항별 응답현황 — "문항별 응답현황" 탭
     * - 배치가 집계한 통계 + 문항/선택지 텍스트를 합쳐 화면용 형태로 반환
     * - 배치가 아직 안 돌았으면 questions = []
     */
    @GetMapping("/questions")
    public ApiResponse<QuestionStatisticsListResponse> getQuestionStatistics(@PathVariable Long surveyId) {
        return ApiResponse.success(
                "문항별 응답현황 조회 성공",
                statisticsQueryService.getQuestionStatistics(surveyId)
        );
    }

    /**
     * 설문 기본정보 요약 — 엑셀(.xlsx) 다운로드
     * - 응답 본문: byte[] (xlsx 바이너리)
     * - 파일명: 설문통계요약_{설문명}_{yyyyMMdd-HHmm}.xlsx
     *   · 한글 안전하게 RFC 5987 형식 (filename* / UTF-8) 으로 전달
     */
    @GetMapping("/export/summary")
    public ResponseEntity<byte[]> exportSummary(@PathVariable Long surveyId) {
        StatisticsSummaryResponse summary = statisticsQueryService.getSummary(surveyId);
        byte[] body = statisticsExcelExporter.exportSummary(summary);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        buildContentDisposition(summary.title()))
                .body(body);
    }

    /**
     * Content-Disposition 값 생성 (한글 파일명 안전 전달).
     * - filename : ASCII 폴백 (구형 클라이언트 대비 고정 문자열)
     * - filename*: RFC 5987 — UTF-8 퍼센트 인코딩 (모던 브라우저가 우선 채택)
     */
    private String buildContentDisposition(String surveyTitle) {
        String filename = "설문통계요약_" + sanitize(surveyTitle)
                + "_" + LocalDateTime.now().format(FILENAME_TIMESTAMP) + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");   // form-encoding 의 '+' → '%20' (RFC 3986 호환)
        return "attachment; filename=\"statistics-summary.xlsx\"; filename*=UTF-8''" + encoded;
    }

    /** 파일명에 사용할 수 없는 문자 치환 (Windows/macOS 양쪽 안전). */
    private String sanitize(String name) {
        return name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
    }
}
