package com.woongjin.survey.domain.statistics.excel.sheet;

import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.excel.ExcelCells;
import com.woongjin.survey.domain.statistics.excel.ExcelStyles;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 시트1: 설문 기본정보 요약 (key-value 2열).
 *
 * [구조]
 *  1행   : 헤더 (항목 / 값)
 *  2~10행: 9개 항목 — surveyId, daysLeft 는 의도적으로 제외
 */
@Component
public class SummarySheetWriter {

    private static final String SHEET_NAME = "설문 요약";
    private static final int COLUMN_COUNT = 2;

    public void write(SXSSFWorkbook wb, ExcelStyles styles, StatisticsSummaryResponse s) {
        SXSSFSheet sheet = wb.createSheet(SHEET_NAME);
        sheet.trackAllColumnsForAutoSizing();

        writeHeader(sheet, styles);
        writeFields(sheet, styles, s);

        ExcelCells.autoSizeAll(sheet, COLUMN_COUNT);
    }

    private void writeHeader(SXSSFSheet sheet, ExcelStyles styles) {
        Row row = sheet.createRow(0);
        ExcelCells.setCell(row, 0, "항목", styles.header());
        ExcelCells.setCell(row, 1, "값",  styles.header());
    }

    private void writeFields(SXSSFSheet sheet, ExcelStyles styles, StatisticsSummaryResponse s) {
        List<Field> fields = List.of(
                new Field("설문명",     s.title()),
                new Field("사이트",     s.site()),
                new Field("시작일",     ExcelCells.formatDateTime(s.beginDate())),
                new Field("종료일",     ExcelCells.formatDateTime(s.endDate())),
                new Field("총 문항 수", s.totalQuestionCount() + "개"),
                new Field("응답 대상",  ExcelCells.formatCount(s.totalTargetCount())),
                new Field("응답 완료",  ExcelCells.formatCount(s.respondedCount())),
                new Field("미응답",     ExcelCells.formatCount(s.notRespondedCount())),
                new Field("응답률",     s.responseRate() + "%")
        );

        int rowIdx = 1;
        for (Field f : fields) {
            Row row = sheet.createRow(rowIdx++);
            ExcelCells.setCell(row, 0, f.key(),   styles.key());
            ExcelCells.setCell(row, 1, f.value(), styles.value());
        }
    }

    private record Field(String key, String value) {}
}
