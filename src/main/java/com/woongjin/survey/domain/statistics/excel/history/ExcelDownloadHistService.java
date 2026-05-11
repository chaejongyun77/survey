package com.woongjin.survey.domain.statistics.excel.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExcelDownloadHistService {

    private static final int PAGE_SIZE = 10;

    private final ExcelDownloadHistRepository histRepository;

    @Transactional
    public void save(Long surveyId) {
        histRepository.save(ExcelDownloadHist.of(surveyId));
    }

    @Transactional(readOnly = true)
    public ExcelDownloadHistSlice getHist(Long surveyId, int page) {
        Slice<ExcelDownloadHistResponse> slice = histRepository
                .findBySurveyIdOrderByCreatedDateDesc(surveyId, PageRequest.of(page, PAGE_SIZE));

        long totalCount = (page == 0) ? histRepository.countBySurveyId(surveyId) : -1;
        return new ExcelDownloadHistSlice(slice.getContent(), slice.hasNext(), totalCount);
    }

    /** 글로벌 어드민 — 모든 설문의 다운로드 이력 페이징 조회 */
    @Transactional(readOnly = true)
    public Page<ExcelDownloadHistListQueryResponse> getAllHistories(ExcelDownloadHistListQueryRequest req) {
        return histRepository.findAllHistories(req.toPageable());
    }
}
