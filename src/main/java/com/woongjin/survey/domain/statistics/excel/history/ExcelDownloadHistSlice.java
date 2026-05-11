package com.woongjin.survey.domain.statistics.excel.history;

import java.util.List;

public record ExcelDownloadHistSlice(
        List<ExcelDownloadHistResponse> items,
        boolean hasNext,
        long totalCount
) { }
