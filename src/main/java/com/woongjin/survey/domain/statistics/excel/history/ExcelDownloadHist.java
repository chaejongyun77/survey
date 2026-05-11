package com.woongjin.survey.domain.statistics.excel.history;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통계 엑셀 다운로드 이력 — EXCEL_DWNLD_HIST_TB 매핑.
 *
 * [성격]
 * - append-only. 수정 없음.
 * - 누가(EMP_ID) 언제(FRST_CRTN_DT) 어떤 설문(SVY_ID)을 다운로드했는지 기록.
 * - employee: EMP_ID 로 조인 — 이름/부서 조회용.
 */
@Getter
@Entity
@Table(name = "EXCEL_DWNLD_HIST_TB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExcelDownloadHist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HIST_ID")
    private Long id;

    @Column(name = "SVY_ID", nullable = false)
    private Long surveyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", nullable = false)
    private Employee employee;

    public String getEmpName() {
        return employee != null ? employee.getEmpName() : null;
    }

    public String getDeptName() {
        return employee != null ? employee.getDepartment().getDeptName() : null;
    }

    public static ExcelDownloadHist of(Long surveyId, Employee employee) {
        ExcelDownloadHist hist = new ExcelDownloadHist();
        hist.surveyId = surveyId;
        hist.employee = employee;
        return hist;
    }
}
