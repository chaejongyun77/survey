package com.woongjin.survey.domain.statistics.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 통계 엑셀 스타일 묶음.
 *
 * [설계 메모]
 * - Workbook 인스턴스당 한 번 생성, 시트들 간 공유
 *   · XLSX 는 워크북당 스타일 개수 제한(64K) 이 있어 재사용이 중요
 * - SheetWriter 들이 styles.xxx() 로 꺼내 씀
 */
public class ExcelStyles {

    private final CellStyle title;
    private final CellStyle header;
    private final CellStyle key;
    private final CellStyle value;
    private final CellStyle valueGray;   // lowRate 등 회색 강조용

    public ExcelStyles(Workbook wb) {
        this.title     = title(wb);
        this.header    = header(wb);
        this.key       = key(wb);
        this.value     = value(wb);
        this.valueGray = valueGray(wb);
    }

    public CellStyle title()     { return title; }
    public CellStyle header()    { return header; }
    public CellStyle key()       { return key; }
    public CellStyle value()     { return value; }
    public CellStyle valueGray() { return valueGray; }

    private static CellStyle title(Workbook wb) {
        CellStyle s = base(wb);
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 13);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle header(Workbook wb) {
        CellStyle s = bordered(wb);
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle key(Workbook wb) {
        CellStyle s = bordered(wb);
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    private static CellStyle value(Workbook wb) {
        CellStyle s = bordered(wb);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    private static CellStyle valueGray(Workbook wb) {
        CellStyle s = value(wb);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private static CellStyle base(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private static CellStyle bordered(Workbook wb) {
        CellStyle s = base(wb);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }
}
