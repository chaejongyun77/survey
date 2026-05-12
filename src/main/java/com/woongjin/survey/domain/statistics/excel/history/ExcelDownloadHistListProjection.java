package com.woongjin.survey.domain.statistics.excel.history;

import java.time.LocalDateTime;

/**
 * 글로벌 어드민 엑셀 다운로드 이력 조회 결과 (Repository 내부 전용).
 * Service 에서 외부 응답 DTO({@link ExcelDownloadHistListQueryResponse}) 로 매핑한다.
 */
public record ExcelDownloadHistListProjection(
        Long surveyId,
        String surveyTitle,
        String empNo,
        String empName,
        String deptName,
        LocalDateTime createdDate
) { }
