package com.woongjin.survey.domain.statistics.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 시트 작성 공통 헬퍼 — 셀/포맷 단위의 작은 유틸 모음.
 */
public final class ExcelCells {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ExcelCells() {}

    /** 셀 1개를 값+스타일로 채움. */
    public static void setCell(Row row, int colIdx, String value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /** 시트 컬럼 N 개에 autoSize 후 최소 너비(chars) 보장.
     *  SXSSF 는 flush된 행은 autoSize 반영 안 되는 경우가 있어 하한선을 명시적으로 설정.
     *  minWidthChars: 최소 너비 (엑셀 문자 단위, 1 unit ≈ 256). */
    public static void autoSizeAll(Sheet sheet, int columnCount, int minWidthChars) {
        int minWidth = minWidthChars * 256;
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            }
        }
    }

    /** "1,250명" 형태. */
    public static String formatCount(int n) {
        return String.format("%,d명", n);
    }

    /** "yyyy-MM-dd HH:mm" 형태. null 안전. */
    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME_FORMATTER);
    }
}
