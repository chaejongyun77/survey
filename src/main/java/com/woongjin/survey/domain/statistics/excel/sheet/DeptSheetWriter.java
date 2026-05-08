package com.woongjin.survey.domain.statistics.excel.sheet;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.excel.ExcelCells;
import com.woongjin.survey.domain.statistics.excel.ExcelStyles;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 시트2: 조직별 응답현황 (raw table).
 *
 * [구조]
 *  1행   : 헤더 — 부서명 / 응답대상 / 응답완료 / 응답률(%)
 *          · 부서ID 는 의도적으로 제외 (관리자 시점에 의미 없음)
 *  2행~  : 부서별 데이터
 *          · lowRate=true 인 행은 회색 배경 (마감일 70% 미만)
 */
@Component
public class DeptSheetWriter {

    private static final String SHEET_NAME = "조직별 응답현황";
    private static final List<String> HEADERS = List.of(
            "부서명", "응답대상", "응답완료", "응답률(%)"
    );

    public void write(SXSSFWorkbook wb, ExcelStyles styles, List<DeptResponseRateResponse> rates) {
        SXSSFSheet sheet = wb.createSheet(SHEET_NAME);
        sheet.trackAllColumnsForAutoSizing();

        writeHeader(sheet, styles);
        writeRows(sheet, styles, rates);

        sheet.createFreezePane(0, 1);
        ExcelCells.autoSizeAll(sheet, HEADERS.size(), 12);
    }

    private void writeHeader(SXSSFSheet sheet, ExcelStyles styles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.size(); i++) {
            ExcelCells.setCell(row, i, HEADERS.get(i), styles.header());
        }
    }

    private void writeRows(SXSSFSheet sheet, ExcelStyles styles, List<DeptResponseRateResponse> rates) {
        for (int i = 0; i < rates.size(); i++) {
            DeptResponseRateResponse d = rates.get(i);
            CellStyle style = d.lowRate() ? styles.valueGray() : styles.value();
            Row row = sheet.createRow(i + 1);

            ExcelCells.setCell(row, 0, d.deptName(),                       style);
            ExcelCells.setCell(row, 1, ExcelCells.formatCount(d.targetCount()),    style);
            ExcelCells.setCell(row, 2, ExcelCells.formatCount(d.respondedCount()), style);
            ExcelCells.setCell(row, 3, d.responseRate() + "%",             style);
        }
    }
}
