package com.woongjin.survey.domain.survey.statistics.dto.projection;

/**
 * 조직별 응답률 — Repository 조회 결과 Projection
 *
 * [역할]
 * QueryDSL 조회로 부서별 (대상자 수, 응답자 수)를 가져온다.
 * 응답률 계산은 Service 레이어에서 수행.
 */
public record DeptResponseRateProjection(
        Long deptId,
        String deptName,
        long targetCount,
        long respondedCount
) {
}
