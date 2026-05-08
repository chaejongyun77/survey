package com.woongjin.survey.domain.statistics.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * 통계 엑셀 스타일 묶음.
 *
 * [설계 메모]
 * - Workbook 인스턴스당 한 번 생성, 시트들 간 공유
 *   · XLSX 는 워크북당 스타일 개수 제한(64K) 이 있어 재사용이 중요
 * - IndexedColors(256색 레거시) 대신 XSSFColor(RGB) 사용
 *   · SXSSFWorkbook → getXSSFWorkbook() 으로 XSSFWorkbook 접근 후 캐스팅
 */
public class ExcelStyles {

    // 컬러 팔레트
    private static final byte[] COLOR_HEADER_BG  = hex(0x3B, 0x82, 0xF6); // 블루-500
    private static final byte[] COLOR_KEY_BG     = hex(0xEF, 0xF6, 0xFF); // 블루-50
    private static final byte[] COLOR_GRAY_BG    = hex(0xF3, 0xF4, 0xF6); // gray-100
    private static final byte[] COLOR_WHITE      = hex(0xFF, 0xFF, 0xFF);
    private static final byte[] COLOR_TEXT_DARK  = hex(0x1F, 0x29, 0x37); // gray-900
    private static final byte[] COLOR_BORDER     = hex(0xD1, 0xD5, 0xDB); // gray-300

    private final CellStyle header;
    private final CellStyle key;
    private final CellStyle value;
    private final CellStyle valueGray;

    public ExcelStyles(Workbook wb) {
        this.header    = header(wb);
        this.key       = key(wb);
        this.value     = value(wb);
        this.valueGray = valueGray(wb);
    }

    public CellStyle header()    { return header; }
    public CellStyle key()       { return key; }
    public CellStyle value()     { return value; }
    public CellStyle valueGray() { return valueGray; }

    private static CellStyle header(Workbook wb) {
        XSSFCellStyle s = xssfBordered(wb);
        s.setFont(createFont(wb, true, COLOR_WHITE));
        setBackground(s, COLOR_HEADER_BG);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle key(Workbook wb) {
        XSSFCellStyle s = xssfBordered(wb);
        s.setFont(createFont(wb, true, COLOR_TEXT_DARK));
        setBackground(s, COLOR_KEY_BG);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    private static CellStyle value(Workbook wb) {
        XSSFCellStyle s = xssfBordered(wb);
        s.setFont(createFont(wb, false, COLOR_TEXT_DARK));
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    private static CellStyle valueGray(Workbook wb) {
        XSSFCellStyle s = (XSSFCellStyle) value(wb);
        setBackground(s, COLOR_GRAY_BG);
        return s;
    }

    private static XSSFCellStyle xssfBase(Workbook wb) {
        XSSFCellStyle s = (XSSFCellStyle) xssf(wb).createCellStyle();
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private static XSSFCellStyle xssfBordered(Workbook wb) {
        XSSFCellStyle s = xssfBase(wb);
        XSSFColor border = new XSSFColor(COLOR_BORDER, null);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setTopBorderColor(border);
        s.setBottomBorderColor(border);
        s.setLeftBorderColor(border);
        s.setRightBorderColor(border);
        return s;
    }

    private static Font createFont(Workbook wb, boolean bold, byte[] color) {
        Font f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) 10);
        setFontColor(wb, f, color);
        return f;
    }

    private static void setBackground(XSSFCellStyle s, byte[] color) {
        s.setFillForegroundColor(new XSSFColor(color, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    /** SXSSFWorkbook → XSSFWorkbook 접근 (RGB 색상 적용에 필요) */
    private static org.apache.poi.xssf.usermodel.XSSFWorkbook xssf(Workbook wb) {
        return ((SXSSFWorkbook) wb).getXSSFWorkbook();
    }

    private static void setFontColor(Workbook wb, Font font, byte[] rgb) {
        ((org.apache.poi.xssf.usermodel.XSSFFont) font)
                .setColor(new XSSFColor(rgb, null));
    }

    private static byte[] hex(int r, int g, int b) {
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }
}
