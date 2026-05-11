package com.woongjin.survey.domain.statistics.excel.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExcelDownloadHistService {

    private final ExcelDownloadHistRepository histRepository;

    @Transactional
    public void save(Long surveyId) {
        histRepository.save(ExcelDownloadHist.of(surveyId));
    }

    /** 글로벌 어드민 — 모든 설문의 다운로드 이력 페이징 조회 */
    @Transactional(readOnly = true)
    public Page<ExcelDownloadHistListQueryResponse> getAllHistories(ExcelDownloadHistListQueryRequest req) {
        return histRepository.findAllPage(req.toPageable())
                .map(p -> new ExcelDownloadHistListQueryResponse(
                        p.surveyId(), p.surveyTitle(),
                        p.empNo(), p.empName(), p.deptName(),
                        p.createdDate()
                ));
    }
}
