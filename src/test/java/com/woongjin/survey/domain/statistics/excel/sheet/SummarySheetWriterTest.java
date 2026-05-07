package com.woongjin.survey.domain.statistics.excel.sheet;

import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.excel.ExcelStyles;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SummarySheetWriter 단위 테스트.
 * - SXSSFWorkbook 으로 작성 → byte[] → XSSFWorkbook 으로 다시 읽어 검증
 */
class SummarySheetWriterTest {

    private final SummarySheetWriter writer = new SummarySheetWriter();

    @Test
    @DisplayName("설문 요약 시트가 생성되고 모든 항목이 채워진다 (surveyId/daysLeft 는 제외)")
    void write_writesAllFields() throws IOException {
        StatisticsSummaryResponse summary = new StatisticsSummaryResponse(
                1042L,
                "2026년 상반기 임직원 만족도 설문",
                "본사",
                LocalDateTime.of(2026, 4, 15, 9, 0),
                LocalDateTime.of(2026, 5, 15, 18, 0),
                5, 1250, 987, 263, 79.0, 8L
        );

        byte[] bytes = writeToBytes(summary);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheet("설문 요약");
            assertThat(sheet).as("시트명: 설문 요약").isNotNull();

            // 1행: 타이틀
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("설문 기본정보 요약");
            // 2행: 헤더
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("항목");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("값");

            // 3~11행: 데이터 9개
            assertCell(sheet, 2,  "설문명",     "2026년 상반기 임직원 만족도 설문");
            assertCell(sheet, 3,  "사이트",     "본사");
            assertCell(sheet, 4,  "시작일",     "2026-04-15 09:00");
            assertCell(sheet, 5,  "종료일",     "2026-05-15 18:00");
            assertCell(sheet, 6,  "총 문항 수", "5개");
            assertCell(sheet, 7,  "응답 대상",  "1,250명");
            assertCell(sheet, 8,  "응답 완료",  "987명");
            assertCell(sheet, 9,  "미응답",     "263명");
            assertCell(sheet, 10, "응답률",    "79.0%");

            // surveyId(1042), daysLeft(8) 는 노출 안 함 → 마지막 행 = 10
            assertThat(sheet.getLastRowNum()).isEqualTo(10);
        }
    }

    @Test
    @DisplayName("응답 대상 0명이어도 정상 생성된다 (분모 0 방어 — 응답률 0.0%)")
    void write_zeroTarget() throws IOException {
        StatisticsSummaryResponse summary = new StatisticsSummaryResponse(
                1L, "테스트 설문", "본사",
                LocalDateTime.of(2026, 4, 15, 9, 0),
                LocalDateTime.of(2026, 5, 15, 18, 0),
                3, 0, 0, 0, 0.0, 8L
        );

        byte[] bytes = writeToBytes(summary);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheet("설문 요약");
            assertCell(sheet, 7,  "응답 대상", "0명");
            assertCell(sheet, 10, "응답률",   "0.0%");
        }
    }

    private byte[] writeToBytes(StatisticsSummaryResponse summary) throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writer.write(wb, new ExcelStyles(wb), summary);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void assertCell(Sheet sheet, int rowIdx, String expectedKey, String expectedValue) {
        assertThat(sheet.getRow(rowIdx).getCell(0).getStringCellValue())
                .as("row %d key", rowIdx).isEqualTo(expectedKey);
        assertThat(sheet.getRow(rowIdx).getCell(1).getStringCellValue())
                .as("row %d value", rowIdx).isEqualTo(expectedValue);
    }
}
