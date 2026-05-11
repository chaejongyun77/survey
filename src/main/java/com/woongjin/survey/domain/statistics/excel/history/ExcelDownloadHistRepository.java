package com.woongjin.survey.domain.statistics.excel.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExcelDownloadHistRepository extends JpaRepository<ExcelDownloadHist, Long> {

    /**
     * Per-survey 이력 — 설문 통계 페이지 모달용.
     * createdBy(=EMP_ID) 로 명시 join, DTO 직접 반환.
     */
    @Query("""
            select new com.woongjin.survey.domain.statistics.excel.history.ExcelDownloadHistResponse(
                e.empName, d.deptName, h.createdDate)
            from ExcelDownloadHist h
            join Employee e on e.id = h.createdBy
            join e.department d
            where h.surveyId = :surveyId
            order by h.createdDate desc
            """)
    Slice<ExcelDownloadHistResponse> findBySurveyIdOrderByCreatedDateDesc(Long surveyId, Pageable pageable);

    long countBySurveyId(Long surveyId);

    /**
     * 글로벌 어드민 이력 — 모든 설문의 다운로드 이력 페이징 조회.
     * 설문명/사번/사원명/부서명 함께 join 후 DTO 직접 반환.
     */
    @Query(
            value = """
                    select new com.woongjin.survey.domain.statistics.excel.history.ExcelDownloadHistListQueryResponse(
                        h.surveyId, s.title, e.empNo, e.empName, d.deptName, h.createdDate)
                    from ExcelDownloadHist h
                    join Survey s on s.id = h.surveyId
                    join Employee e on e.id = h.createdBy
                    join e.department d
                    """,
            countQuery = "select count(h) from ExcelDownloadHist h"
    )
    Page<ExcelDownloadHistListQueryResponse> findAllHistories(Pageable pageable);
}

