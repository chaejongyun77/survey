package com.woongjin.survey.domain.statistics.excel.history;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExcelDownloadHistService {

    private static final int PAGE_SIZE = 10;

    private final ExcelDownloadHistRepository histRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void save(Long surveyId, Long empId) {
        Employee employee = employeeRepository.getReferenceById(empId);
        histRepository.save(ExcelDownloadHist.of(surveyId, employee));
    }

    @Transactional(readOnly = true)
    public ExcelDownloadHistSlice getHist(Long surveyId, int page) {
        Slice<ExcelDownloadHistResponse> slice = histRepository
                .findBySurveyIdOrderByCreatedDateDesc(surveyId, PageRequest.of(page, PAGE_SIZE))
                .map(h -> new ExcelDownloadHistResponse(h.getEmpName(), h.getDeptName(), h.getCreatedDate()));

        long totalCount = (page == 0) ? histRepository.countBySurveyId(surveyId) : -1;
        return new ExcelDownloadHistSlice(slice.getContent(), slice.hasNext(), totalCount);
    }
}
