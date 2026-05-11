package com.woongjin.survey.domain.statistics.excel.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 엑셀 다운로드 이력 — QueryDSL 기반 동적 조회 전용 인터페이스.
 *
 * 기본 CRUD 는 {@link ExcelDownloadHistRepository} 의 JpaRepository 상속분에 남겨둔다.
 */
public interface ExcelDownloadHistRepositoryCustom {

    /**
     * 글로벌 어드민 이력 — 모든 설문의 다운로드 이력을 페이징 조회.
     * 정렬은 Pageable.getSort() 를 따른다 (Service 단에서 createdDate DESC 부여).
     */
    Page<ExcelDownloadHistListProjection> findAllPage(Pageable pageable);
}
