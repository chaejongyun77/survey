package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;

/**
 * 조직별 응답률 — 응답 DTO
 *
 * [화면 매핑]
 *  조직명 | 응답률 바 | 비율(%) | 응답수(46/50명)
 *
 * [필드 메모]
 * - lowRate: "마감일 70% 미만 조직은 회색 표시" 룰을 위한 플래그
 *           프론트에서 직접 계산해도 되지만 서버에서 일관 적용
 */
public record DeptResponseRateResponse(
        Long deptId,
        String deptName,
        int targetCount,
        int respondedCount,
        double responseRate,   // %, 소수 첫째자리
        boolean lowRate        // 회색 표시 여부
) {
    public static DeptResponseRateResponse from(DeptResponseRateProjection p, double responseRate, boolean lowRate) {
        return new DeptResponseRateResponse(
                p.deptId(), p.deptName(), (int) p.targetCount(), (int) p.respondedCount(), responseRate, lowRate);
    }
}
