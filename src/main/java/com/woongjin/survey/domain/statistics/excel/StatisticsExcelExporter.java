package com.woongjin.survey.domain.statistics.excel;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.excel.sheet.DeptSheetWriter;
import com.woongjin.survey.domain.statistics.excel.sheet.SummarySheetWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * 통계 엑셀 변환기 — 시트 작성기들을 조립해 .xlsx 바이트로 출력.
 *
 * [구조]
 * - 시트별 작성 책임은 sheet/*SheetWriter 로 분리
 * - 본 클래스는 워크북 생성/쓰기 보일러플레이트와 시트 조립만 담당
 *
 * [확장]
 * - 시트3, 4 추가 시: SheetWriter 주입 + exportAll() 파라미터/호출 한 줄씩 추가
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsExcelExporter {

    private final SummarySheetWriter summarySheetWriter;
    private final DeptSheetWriter deptSheetWriter;

    /** 통계 페이지 전체(현재까지 구현된 시트들) → 단일 워크북 .xlsx */
    public byte[] exportAll(StatisticsSummaryResponse summary,
                            List<DeptResponseRateResponse> depts) {
        return build(wb -> {
            ExcelStyles styles = new ExcelStyles(wb);
            summarySheetWriter.write(wb, styles, summary);
            deptSheetWriter.write(wb, styles, depts);
        });
    }

    /** SXSSFWorkbook 생성/쓰기 보일러플레이트 — 시트 조립 람다만 받아 byte[] 로 마무리. */
    private byte[] build(Consumer<SXSSFWorkbook> sheetAssembler) {
        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            sheetAssembler.accept(wb);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("설문 통계 엑셀 생성 실패", e);
            throw new UncheckedIOException("설문 통계 엑셀 생성 실패", e);
        }
    }
}
