package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.survey.dto.SurveyIntroDto;
import com.woongjin.survey.domain.survey.infra.SurveyTokenRepository;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 설문 임시 토큰 서비스
 * - 8081 요청 시 empNo 기반으로 진행중 설문 조회 + 토큰 발급
 * - Redis OTP 방식 (TTL 1분, 단발성)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyCreateService {

    private final EmployeeRepository employeeRepository;
    private final SurveyQueryService surveyQueryService;
    private final SurveyTokenRepository surveyTokenRepository;

    /**
     * 사원번호 기반 설문 토큰 발급
     *
     * 1) empNo → Employee 조회 (없으면 설문 없음)
     * 2) 진행중 설문 조회 (없으면 설문 없음)
     * 3) 토큰 발급 후 Redis 저장 (TTL 1분)
     */
    @Transactional
    public ApiResponse<String> issue(String empNo) {

        Optional<Employee> employeeOpt = employeeRepository.findByEmpNo(empNo);
        if (employeeOpt.isEmpty()) {
            log.debug("설문 토큰 발급 거부: 존재하지 않는 사원번호 empNo={}", empNo);
            return ApiResponse.success("대상 설문 없음");
        }

        Employee employee = employeeOpt.get();

        Optional<SurveyIntroDto> surveyOpt = surveyQueryService.findActiveSurveyByEmpId(employee.getId());
        if (surveyOpt.isEmpty()) {
            log.debug("설문 토큰 발급 거부: 진행중인 설문 없음 empNo={}, empId={}", empNo, employee.getId());
            return ApiResponse.success("대상 설문 없음");
        }

        SurveyIntroDto survey = surveyOpt.get();
        String token = surveyTokenRepository.save(empNo, survey.getSurveyId());
        log.info("설문 토큰 발급 완료: empNo={}, surveyId={}", empNo, survey.getSurveyId());

        return ApiResponse.success("설문 토큰 발급 성공", token);
    }
}
