package com.woongjin.survey.domain.statistics.excel.sheet;

import com.woongjin.survey.domain.statistics.dto.QuestionItemMetaDto;
import com.woongjin.survey.domain.statistics.dto.QuestionMetaDto;
import com.woongjin.survey.domain.statistics.dto.RespondentAnswerDto;
import com.woongjin.survey.domain.statistics.excel.ExcelCells;
import com.woongjin.survey.domain.statistics.excel.ExcelStyles;
import com.woongjin.survey.domain.statistics.dto.ResponseListResponse;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 시트3: 응답자별 문항 답변 (raw table).
 *
 * [컬럼 구성]
 *  고정 3열 : 응답자명 / 부서명 / 응답일시
 *  동적 N열 : 문항 순서대로 1문항 = 1열 (문항명이 헤더)
 *
 * [셀 변환 정책]
 *  SINGLE_CHOICE   : 선택 항목명 1개
 *  MULTIPLE_CHOICE : 선택 항목명들, 쉼표 연결
 *  RANKING         : "1순위:A, 2순위:B, ..." 형태
 *  SCALE           : 척도값 숫자 문자열
 *  SUBJECTIVE      : 주관식 텍스트 그대로
 *  미응답           : 빈 문자열
 */
@Component
public class RespondentSheetWriter {

    private static final String SHEET_NAME = "응답자별 문항 답변";

    // 고정 헤더 3개
    private static final List<String> FIXED_HEADERS = List.of("응답자명", "부서명", "응답일시");
    private static final int FIXED_COL_COUNT = FIXED_HEADERS.size();

    public void write(SXSSFWorkbook wb, ExcelStyles styles, ResponseListResponse data) {
        SXSSFSheet sheet = wb.createSheet(SHEET_NAME);
        sheet.trackAllColumnsForAutoSizing();

        List<QuestionMetaDto> questions = data.questions();
        List<RespondentAnswerDto> responses = data.responses();

        // questionId → itemId → itemName 역색인 (답변 변환용)
        Map<Long, Map<Long, String>> itemNameByQuestionAndItemId = buildItemIndex(questions);

        writeHeader(sheet, styles, questions);
        writeRows(sheet, styles, responses, questions, itemNameByQuestionAndItemId);

        sheet.createFreezePane(0, 1);
        ExcelCells.autoSizeAll(sheet, FIXED_COL_COUNT + questions.size(), 16);
    }

    private void writeHeader(SXSSFSheet sheet, ExcelStyles styles, List<QuestionMetaDto> questions) {
        Row row = sheet.createRow(0);
        CellStyle headerStyle = styles.header();

        // 고정 헤더
        for (int i = 0; i < FIXED_HEADERS.size(); i++) {
            ExcelCells.setCell(row, i, FIXED_HEADERS.get(i), headerStyle);
        }

        // 동적 헤더 (문항명)
        for (int i = 0; i < questions.size(); i++) {
            ExcelCells.setCell(row, FIXED_COL_COUNT + i, questions.get(i).questionName(), headerStyle);
        }
    }

    private void writeRows(SXSSFSheet sheet, ExcelStyles styles,
                           List<RespondentAnswerDto> responses,
                           List<QuestionMetaDto> questions,
                           Map<Long, Map<Long, String>> itemIndex) {

        CellStyle valueStyle = styles.value();

        for (int rowIdx = 0; rowIdx < responses.size(); rowIdx++) {
            RespondentAnswerDto resp = responses.get(rowIdx);
            Row row = sheet.createRow(rowIdx + 1);

            // 고정 3열
            ExcelCells.setCell(row, 0, resp.empName(),                          valueStyle);
            ExcelCells.setCell(row, 1, resp.deptName(),                         valueStyle);
            ExcelCells.setCell(row, 2, ExcelCells.formatDateTime(resp.submittedAt()), valueStyle);

            // 답변 역색인 — questionId → SurveyAnswerDto
            Map<Long, SurveyAnswerDto> answerByQuestionId = resp.answers().stream()
                    .collect(Collectors.toMap(SurveyAnswerDto::getQuestionId, a -> a));

            // 동적 문항 열
            for (int col = 0; col < questions.size(); col++) {
                QuestionMetaDto q = questions.get(col);
                SurveyAnswerDto answer = answerByQuestionId.get(q.questionId());
                String cellValue = answer == null ? "" : formatAnswer(answer, q, itemIndex);
                ExcelCells.setCell(row, FIXED_COL_COUNT + col, cellValue, valueStyle);
            }
        }
    }

    /** 답변 → 표시 문자열 변환 (문항 유형별 분기) */
    private String formatAnswer(SurveyAnswerDto answer, QuestionMetaDto meta,
                                Map<Long, Map<Long, String>> itemIndex) {
        QuestionType type = answer.getType();
        Map<Long, String> itemNames = itemIndex.getOrDefault(meta.questionId(), Map.of());

        return switch (type) {
            case SINGLE_CHOICE, MULTIPLE_CHOICE -> {
                List<Long> ids = answer.getSelectedItemIds();
                if (ids == null || ids.isEmpty()) yield "";
                yield ids.stream()
                        .map(id -> itemNames.getOrDefault(id, "(알 수 없음)"))
                        .collect(Collectors.joining(", "));
            }
            case RANKING -> {
                List<Long> ranked = answer.getRankedItemIds();
                if (ranked == null || ranked.isEmpty()) yield "";
                yield IntStream.range(0, ranked.size())
                        .mapToObj(i -> (i + 1) + "순위:" + itemNames.getOrDefault(ranked.get(i), "(알 수 없음)"))
                        .collect(Collectors.joining(", "));
            }
            case SCALE -> answer.getScaleValue() == null ? "" : String.valueOf(answer.getScaleValue());
            case SUBJECTIVE -> answer.getTextAnswer() == null ? "" : answer.getTextAnswer();
        };
    }

    /** questionId → (itemId → itemName) 역색인 생성 */
    private Map<Long, Map<Long, String>> buildItemIndex(List<QuestionMetaDto> questions) {
        return questions.stream()
                .collect(Collectors.toMap(
                        QuestionMetaDto::questionId,
                        q -> q.items().stream()
                                .collect(Collectors.toMap(
                                        QuestionItemMetaDto::itemId,
                                        QuestionItemMetaDto::itemName
                                ))
                ));
    }
}
