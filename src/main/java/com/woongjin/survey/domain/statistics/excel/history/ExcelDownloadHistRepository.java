package com.woongjin.survey.domain.statistics.excel.history;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 엑셀 다운로드 이력 Repository.
 *
 *  - JpaRepository  : 기본 CRUD
 *  - Custom         : QueryDSL 동적 조회 ({@link ExcelDownloadHistRepositoryCustom})
 */
public interface ExcelDownloadHistRepository
        extends JpaRepository<ExcelDownloadHist, Long>, ExcelDownloadHistRepositoryCustom {
}

