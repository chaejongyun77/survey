package com.woongjin.survey.domain.statistics.excel.history;

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
 * - 누가(FRST_CRTN_ID) 언제(FRST_CRTN_DT) 어떤 설문(SVY_ID)을 다운로드했는지 기록.
 * - 다운로더 식별은 BaseEntity.createdBy(@CreatedBy) 로 자동 세팅됨.
 * - 사원명/부서명은 이력 조회 시 repository 쿼리에서 emp_tb join 으로 가져옴
 *   (같은 컬럼에 @JoinColumn 을 또 걸면 @CreatedBy 와 충돌해 INSERT 가 깨짐).
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

    public static ExcelDownloadHist of(Long surveyId) {
        ExcelDownloadHist hist = new ExcelDownloadHist();
        hist.surveyId = surveyId;
        return hist;
    }
}
