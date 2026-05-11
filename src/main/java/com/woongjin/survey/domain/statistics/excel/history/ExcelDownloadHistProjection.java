package com.woongjin.survey.domain.statistics.excel.history;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

/**
 * per-survey 엑셀 다운로드 이력 조회 결과 (Repository 내부 전용).
 * Service 에서 외부 응답 DTO({@link ExcelDownloadHistResponse}) 로 매핑한다.
 */
public record ExcelDownloadHistProjection(
        String empName,
        String deptName,
        LocalDateTime createdDate
) {
    @QueryProjection
    public ExcelDownloadHistProjection { }
}
