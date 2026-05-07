package com.woongjin.survey.domain.statistics.excel.sheet;

import com.woongjin.survey.domain.statistics.dto.QuestionStatItemResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionStatisticsListResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionStatisticsResponse;
import com.woongjin.survey.domain.statistics.excel.ExcelCells;
import com.woongjin.survey.domain.statistics.excel.ExcelStyles;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 시트4: 문항별 응답현황.
 *
 * [구조]
 *  문항마다 블록 하나:
 *   - 타이틀 행 : "Q{sortIdx}. {문항명}" (A:C 병합)
 *   - 헤더 행   : 선택지 / 응답수 / 비율(%)
 *   - 데이터 행 : 선택지별 카운트
 *     · SCALE 만 마지막에 "평균 점수" 행 추가
 *     · SUBJECTIVE 는 선택지 없이 샘플 텍스트 나열
 *   - 구분 빈 행 : 문항 블록 사이
 *
 * [배치 미집계 처리]
 *  questions 가 빈 리스트면 안내 메시지 1행만 출력
 */
@Component
public class QuestionStatSheetWriter {

    private static final String SHEET_NAME = "문항별 응답현황";
    private static final int COLUMN_COUNT = 3;

    public void write(SXSSFWorkbook wb, ExcelStyles styles, QuestionStatisticsListResponse data) {
        SXSSFSheet sheet = wb.createSheet(SHEET_NAME);
        sheet.trackAllColumnsForAutoSizing();

        List<QuestionStatisticsResponse> questions = data.questions();

        if (questions.isEmpty()) {
            ExcelCells.setCell(sheet.createRow(0), 0,
                    "집계된 통계 데이터가 없습니다. 배치 실행 후 다시 시도하세요.", styles.value());
            ExcelCells.autoSizeAll(sheet, COLUMN_COUNT);
            return;
        }

        int rowIdx = 0;
        for (int i = 0; i < questions.size(); i++) {
            rowIdx = writeQuestionBlock(sheet, styles, questions.get(i), i + 1, rowIdx);
            sheet.createRow(rowIdx++); // 문항 간 빈 행
        }

        ExcelCells.autoSizeAll(sheet, COLUMN_COUNT);
    }

    /** 문항 블록 1개 작성 → 다음 rowIdx 반환 */
    private int writeQuestionBlock(SXSSFSheet sheet, ExcelStyles styles,
                                   QuestionStatisticsResponse q, int questionNo, int rowIdx) {
        // 타이틀 행
        Row titleRow = sheet.createRow(rowIdx++);
        String title = "Q" + questionNo + ". " + q.title()
                + "  [총 " + q.totalResponseCount() + "명 응답]";
        ExcelCells.setCell(titleRow, 0, title, styles.key());
        sheet.addMergedRegion(new CellRangeAddress(
                titleRow.getRowNum(), titleRow.getRowNum(), 0, COLUMN_COUNT - 1));

        if (q.questionType() == QuestionType.SUBJECTIVE) {
            rowIdx = writeSubjectiveRows(sheet, styles, q.sampleTexts(), rowIdx);
        } else {
            rowIdx = writeChoiceRows(sheet, styles, q, rowIdx);
        }

        return rowIdx;
    }

    /** SINGLE_CHOICE / MULTIPLE_CHOICE / RANKING / SCALE — 선택지 테이블 */
    private int writeChoiceRows(SXSSFSheet sheet, ExcelStyles styles,
                                QuestionStatisticsResponse q, int rowIdx) {
        // 헤더
        Row header = sheet.createRow(rowIdx++);
        ExcelCells.setCell(header, 0, "선택지",   styles.header());
        ExcelCells.setCell(header, 1, "응답수",   styles.header());
        ExcelCells.setCell(header, 2, "비율(%)", styles.header());

        // 데이터
        for (QuestionStatItemResponse item : q.items()) {
            Row row = sheet.createRow(rowIdx++);
            ExcelCells.setCell(row, 0, item.label(),                      styles.value());
            ExcelCells.setCell(row, 1, String.valueOf(item.count()),       styles.value());
            ExcelCells.setCell(row, 2, item.percentage() + "%",           styles.value());
        }

        // SCALE 전용 — 평균 점수 추가 행
        if (q.questionType() == QuestionType.SCALE && q.average() != null) {
            Row avgRow = sheet.createRow(rowIdx++);
            ExcelCells.setCell(avgRow, 0, "평균 점수", styles.key());
            ExcelCells.setCell(avgRow, 1, q.average() + "점", styles.value());
            ExcelCells.setCell(avgRow, 2, "", styles.value());
        }

        return rowIdx;
    }

    /** SUBJECTIVE — 샘플 텍스트 나열 */
    private int writeSubjectiveRows(SXSSFSheet sheet, ExcelStyles styles,
                                    List<String> sampleTexts, int rowIdx) {
        Row header = sheet.createRow(rowIdx++);
        ExcelCells.setCell(header, 0, "주관식 답변 (샘플)", styles.header());
        sheet.addMergedRegion(new CellRangeAddress(
                header.getRowNum(), header.getRowNum(), 0, COLUMN_COUNT - 1));

        if (sampleTexts == null || sampleTexts.isEmpty()) {
            Row row = sheet.createRow(rowIdx++);
            ExcelCells.setCell(row, 0, "(답변 없음)", styles.value());
            sheet.addMergedRegion(new CellRangeAddress(
                    row.getRowNum(), row.getRowNum(), 0, COLUMN_COUNT - 1));
        } else {
            for (String text : sampleTexts) {
                Row row = sheet.createRow(rowIdx++);
                ExcelCells.setCell(row, 0, text, styles.value());
                sheet.addMergedRegion(new CellRangeAddress(
                        row.getRowNum(), row.getRowNum(), 0, COLUMN_COUNT - 1));
            }
        }

        return rowIdx;
    }
}
