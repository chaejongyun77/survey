package com.woongjin.survey.domain.statistics.excel.history;

import java.time.LocalDateTime;

public record ExcelDownloadHistListQueryResponse(
        Long surveyId,
        String surveyTitle,
        String empNo,
        String empName,
        String deptName,
        LocalDateTime createdDate
) { }
