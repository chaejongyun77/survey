package com.woongjin.survey.domain.statistics.excel.history;

import java.time.LocalDateTime;

public record ExcelDownloadHistResponse(
        String empName,
        String deptName,
        LocalDateTime createdDate
) { }
