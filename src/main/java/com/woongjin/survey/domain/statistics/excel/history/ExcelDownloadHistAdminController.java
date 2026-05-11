package com.woongjin.survey.domain.statistics.excel.history;

import com.woongjin.survey.domain.noti.PageQueryResponse;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 엑셀 다운로드 이력 — 글로벌 어드민 조회 API.
 * 모든 설문의 다운로드 이력을 페이징으로 조회.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/external/v1/admin")
public class ExcelDownloadHistAdminController {

    private final ExcelDownloadHistService excelDownloadHistService;

    @GetMapping("/excel-downloads/histories")
    public ApiResponse<PageQueryResponse<ExcelDownloadHistListQueryResponse>> getHistories(
            @ModelAttribute ExcelDownloadHistListQueryRequest req) {
        Page<ExcelDownloadHistListQueryResponse> histories = excelDownloadHistService.getAllHistories(req);
        return ApiResponse.success(
                "엑셀 다운로드 이력 조회 성공",
                new PageQueryResponse<>(histories)
        );
    }
}
