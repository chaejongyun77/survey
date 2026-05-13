package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;

import java.util.ArrayList;
import java.util.List;

/**
 * 조직별 응답률 — 응답 DTO (계층 트리)
 *
 * [화면 매핑]
 *  상위 부서(LVL=1) 행 + 하위 부서(LVL=2) children 행
 *
 * [필드 메모]
 * - children : 하위 부서 노드. leaf 면 빈 리스트
 * - lowRate  : 마감일 70% 미만 회색 표시 플래그 (부모/자식 동일 규칙)
 */
public record DeptResponseRateResponse(
        Long deptId,
        String deptName,
        int deptLvl,
        int targetCount,
        int respondedCount,
        double responseRate,   // %, 소수 첫째자리
        boolean lowRate,
        List<DeptResponseRateResponse> children
) {
    private static final double LOW_RATE_THRESHOLD = 70.0;

    /** 직원이 직접 속한 leaf 부서 (보통 LVL=2) */
    public static DeptResponseRateResponse leaf(DeptResponseRateProjection p, boolean isDeadlineToday) {
        int lvl = p.deptLvl() == null ? 2 : p.deptLvl();
        return build(p.deptId(), p.deptName(), lvl,
                (int) p.targetCount(), (int) p.respondedCount(),
                isDeadlineToday, List.of());
    }

    /** 하위 leaf 들을 합산하여 만든 상위 부서 노드 (LVL=1) */
    public static DeptResponseRateResponse parent(
            Long deptId, String deptName,
            int totalTarget, int totalResponded,
            List<DeptResponseRateResponse> children,
            boolean isDeadlineToday) {
        return build(deptId, deptName, 1, totalTarget, totalResponded, isDeadlineToday, children);
    }

    private static DeptResponseRateResponse build(
            Long id, String name, int lvl,
            int target, int responded,
            boolean isDeadlineToday,
            List<DeptResponseRateResponse> children) {
        double rate = target == 0 ? 0.0 : Math.round((double) responded / target * 1000) / 10.0;
        boolean lowRate = isDeadlineToday && rate < LOW_RATE_THRESHOLD;
        return new DeptResponseRateResponse(id, name, lvl, target, responded, rate, lowRate, children);
    }

    /**
     * 트리를 평탄화해서 leaf 노드만 수집.
     * 엑셀 출력은 직원 단위 집계 행만 표시하므로 부모 행을 제외한다.
     */
    public static List<DeptResponseRateResponse> flattenLeaves(List<DeptResponseRateResponse> tree) {
        List<DeptResponseRateResponse> out = new ArrayList<>();
        collectLeaves(tree, out);
        return out;
    }

    private static void collectLeaves(List<DeptResponseRateResponse> nodes,
                                      List<DeptResponseRateResponse> out) {
        for (DeptResponseRateResponse node : nodes) {
            if (node.children().isEmpty()) {
                out.add(node);
            } else {
                collectLeaves(node.children(), out);
            }
        }
    }
}
