package com.woongjin.survey.domain.statistics.excel.history;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExcelDownloadHistRepository extends JpaRepository<ExcelDownloadHist, Long> {

    @Query("""
            select h from ExcelDownloadHist h
            join fetch h.employee e
            join fetch e.department
            where h.surveyId = :surveyId
            order by h.createdDate desc
            """)
    Slice<ExcelDownloadHist> findBySurveyIdOrderByCreatedDateDesc(Long surveyId, Pageable pageable);

    long countBySurveyId(Long surveyId);
}
