package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;

public record DeptResponseRateResponse(
        Long deptId,
        String deptName,
        int targetCount,
        int respondedCount,
        double responseRate,   // %, 소수 첫째자리
        boolean lowRate        // 마감일 당일 70% 미만이면 회색 표시
) {
    private static final double LOW_RATE_THRESHOLD = 70.0;

    public static DeptResponseRateResponse from(DeptResponseRateProjection p, boolean isDeadlineToday) {
        int targetCnt    = (int) p.targetCount();
        int respondedCnt = (int) p.respondedCount();
        double rate      = targetCnt == 0 ? 0.0 : Math.round((double) respondedCnt / targetCnt * 1000) / 10.0;
        boolean lowRate  = isDeadlineToday && rate < LOW_RATE_THRESHOLD;

        return new DeptResponseRateResponse(
                p.deptId(),
                p.deptName(),
                targetCnt,
                respondedCnt,
                rate,
                lowRate
        );
    }
}
