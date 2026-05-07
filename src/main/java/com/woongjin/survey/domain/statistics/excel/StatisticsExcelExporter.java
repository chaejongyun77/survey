package com.woongjin.survey.domain.statistics.excel;

import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 설문 통계 엑셀 변환기 — DTO → byte[] (.xlsx)
 *
 * [설계 메모]
 * - SXSSFWorkbook 사용 — 추후 응답자별 시트 대용량 대응
 *   · 시트1(설문 요약)만으론 과할 수 있으나, 시트 추가 시점에 워크북 종류를 갈아끼우면 코드가 흩어짐
 * - 시트 단위로 메서드 분리 — 추후 시트2~4 추가 시 writeXxxSheet() 만 추가
 * - 컨트롤러는 byte[] 만 받아 ResponseEntity 로 래핑
 */
@Slf4j
@Component
public class StatisticsExcelExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 설문 기본정보 요약 시트 1개짜리 워크북. */
    public byte[] exportSummary(StatisticsSummaryResponse summary) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            writeSummarySheet(workbook, summary);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("설문 통계 엑셀 생성 실패: surveyId={}", summary.surveyId(), e);
            throw new UncheckedIOException("설문 통계 엑셀 생성 실패", e);
        }
    }

    private void writeSummarySheet(SXSSFWorkbook workbook, StatisticsSummaryResponse s) {
        SXSSFSheet sheet = workbook.createSheet("설문 요약");
        sheet.trackAllColumnsForAutoSizing();   // SXSSF 에서 autoSizeColumn 쓰려면 필수

        CellStyle titleStyle  = titleStyle(workbook);
        CellStyle headerStyle = headerStyle(workbook);
        CellStyle keyStyle    = keyStyle(workbook);
        CellStyle valueStyle  = valueStyle(workbook);

        // 1행: 타이틀 (A:B 병합)
        Row titleRow = sheet.createRow(0);
        setCell(titleRow, 0, "설문 기본정보 요약", titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // 2행: 헤더
        Row headerRow = sheet.createRow(1);
        setCell(headerRow, 0, "항목", headerStyle);
        setCell(headerRow, 1, "값",  headerStyle);

        // 3행~: 데이터 9개
        int rowIdx = 2;
        writePair(sheet, rowIdx++, "설문명",     s.title(),                          keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "사이트",     s.site(),                           keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "시작일",     formatDateTime(s.beginDate()),      keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "종료일",     formatDateTime(s.endDate()),        keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "총 문항 수", s.totalQuestionCount() + "개",      keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "응답 대상",  formatCount(s.totalTargetCount()),  keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "응답 완료",  formatCount(s.respondedCount()),    keyStyle, valueStyle);
        writePair(sheet, rowIdx++, "미응답",     formatCount(s.notRespondedCount()), keyStyle, valueStyle);
        writePair(sheet, rowIdx,   "응답률",     s.responseRate() + "%",             keyStyle, valueStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writePair(SXSSFSheet sheet, int rowIdx, String key, String value,
                           CellStyle keyStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIdx);
        setCell(row, 0, key,   keyStyle);
        setCell(row, 1, value, valueStyle);
    }

    private void setCell(Row row, int colIdx, String value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME_FORMATTER);
    }

    private static String formatCount(int n) {
        return String.format("%,d명", n);
    }

    // ─────────── 스타일 ───────────

    private CellStyle titleStyle(Workbook wb) {
        CellStyle style = baseStyle(wb);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = borderedStyle(wb);
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle keyStyle(Workbook wb) {
        CellStyle style = borderedStyle(wb);
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle valueStyle(Workbook wb) {
        CellStyle style = borderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle baseStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle borderedStyle(Workbook wb) {
        CellStyle style = baseStyle(wb);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
