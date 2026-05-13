package com.woongjin.survey.domain.statistics.dto.projection;

/**
 * 조직별 응답률 — Repository 조회 결과 Projection
 *
 * [역할]
 * QueryDSL 조회로 부서별 (대상자 수, 응답자 수) 및 계층 정보를 가져온다.
 * 응답률 계산과 트리 조립은 Service 레이어에서 수행.
 *
 * [계층 필드]
 * - deptLvl       : 부서 단계 (1=부문, 2=하위 부서)
 * - parentDeptId  : 상위 부서 ID. LVL=1(최상위)이면 null
 * - parentDeptName: 상위 부서명. LVL=1 이면 null
 */
public record DeptResponseRateProjection(
        Long deptId,
        String deptName,
        Integer deptLvl,
        Long parentDeptId,
        String parentDeptName,
        long targetCount,
        long respondedCount
) {
}
