package com.woongjin.survey.domain.statistics.excel.history;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExcelDownloadHistRepository extends JpaRepository<ExcelDownloadHist, Long> {

    /**
     * Employee 는 엔티티 그래프에 없으므로 createdBy(=EMP_ID) 로 명시적 join.
     * 이름/부서는 DTO 로 바로 받아서 N+1 / lazy 이슈 차단.
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
}
