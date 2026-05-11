package com.woongjin.survey.domain.statistics.excel.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 엑셀 다운로드 이력 — 어드민 팝업 View 컨트롤러.
 * nav 의 "엑셀 이력 관리" 클릭 시 새 창으로 열림.
 */
@Controller
@RequiredArgsConstructor
public class ExcelDownloadHistAdminViewController {

    @GetMapping("/surveys/admin/histories/excel")
    public String page() {
        return "survey/excel-modal";
    }
}
