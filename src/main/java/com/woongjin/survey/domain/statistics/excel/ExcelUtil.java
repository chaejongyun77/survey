package com.woongjin.survey.domain.statistics.excel;

import org.springframework.http.ContentDisposition;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ExcelUtil {

    private static final DateTimeFormatter FILENAME_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private ExcelUtil() {}

    /** 한글 파일명 안전 전달을 위한 Content-Disposition 헤더 값 생성 (RFC 5987). */
    public static String attachmentHeader(String surveyTitle) {
        String safe = surveyTitle == null ? "" : surveyTitle.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
        String filename = "설문통계_" + safe + "_" + LocalDateTime.now().format(FILENAME_TIMESTAMP) + ".xlsx";
        return ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
