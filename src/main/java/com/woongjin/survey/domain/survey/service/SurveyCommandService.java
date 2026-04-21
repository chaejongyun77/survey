package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResult;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;
import com.woongjin.survey.domain.survey.infra.SurveyTokenRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 설문 커맨드 서비스
 * - 8081 요청 시 empNo 기반으로 진행중 설문 조회 + Redis 토큰 발급 (issue)
 * - 설문 인트로 진입 시 Redis 토큰 검증 + Client JWT 발급 (processIntroToken)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyCommandService {

    private final EmployeeRepository           employeeRepository;
    private final SurveyQueryService           surveyQueryService;
    private final SurveyTokenRepository        surveyTokenRepository;
    private final SurveyParticipationValidator participationValidator;
    private final ClientTokenProvider          clientTokenProvider;

    // ─────────────────────────────────────────────────────────────
    // 외부 시스템(8081) 요청 — Redis 설문 토큰 발급
    // ─────────────────────────────────────────────────────────────

    /**
     * 사원번호 기반 설문 토큰 발급
     *
     * [검증 순서]
     * 1) empNo → Employee 조회 (없으면 발급 거부)
     * 2) 진행중 + 대상자로 등록된 설문 조회 (없으면 발급 거부)
     * 3) 이미 응답 완료 여부 확인 (완료했으면 발급 거부)
     * 4) 토큰 발급 후 Redis 저장 (TTL 1분)
     */
    @Transactional(readOnly = true)
    public ApiResponse<String> issue(String empNo) {

        // 1) Employee 조회
        Employee employee = employeeRepository.findByEmpNo(empNo)
                .orElseThrow(() -> {
                    log.debug("설문 토큰 발급 거부: 존재하지 않는 사원번호 empNo={}", empNo);
                    return new BusinessException(ErrorCode.SURVEY_NOT_TARGET);
                });

        // 2) 진행중 설문 조회 (기간, 상태, 대상자 등록 여부 포함)
        SurveyIntroResponse survey = surveyQueryService.findActiveSurveyByEmpId(employee.getId())
                .orElseThrow(() -> {
                    log.debug("설문 토큰 발급 거부: 진행중인 설문 없음 empNo={}, empId={}", empNo, employee.getId());
                    return new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
                });

        // 3) 이미 응답 완료 여부 확인
        if (participationValidator.hasAlreadySubmitted(survey.getSurveyId(), employee.getId())) {
            log.debug("설문 토큰 발급 거부: 이미 응답 완료 empNo={}, surveyId={}", empNo, survey.getSurveyId());
            throw new BusinessException(ErrorCode.SURVEY_ALREADY_DONE);
        }

        // 4) Redis 토큰 발급
        String token = surveyTokenRepository.save(empNo, survey.getSurveyId());
        log.info("설문 토큰 발급 완료: empNo={}, surveyId={}", empNo, survey.getSurveyId());

        return ApiResponse.success("설문 토큰 발급 성공", token);
    }

    // ─────────────────────────────────────────────────────────────
    // 설문 인트로 진입 — Client JWT 발급
    // ─────────────────────────────────────────────────────────────

    /**
     * Redis 토큰 검증 + Client JWT 발급
     *
     * [처리 흐름]
     * 1) Redis 에서 token 조회 → payload(empNo:surveyId) 복원
     * 2) 조회 즉시 Redis 토큰 폐기 (단발성 보장)
     * 3) payload 파싱 + 유효성 검증
     * 4) empNo → empId 변환
     * 5) Client JWT 발급
     *
     * [설계 포인트]
     * HttpServletResponse 를 받지 않음 — 쿠키를 심는 행위는 Controller 책임
     * Service 는 필요한 데이터(clientToken, surveyId)를 SurveyIntroContext 로 반환
     *
     * @param token URL ?token= 으로 전달된 Redis 일회용 토큰
     * @return SurveyIntroContext (clientToken + surveyId)
     * @throws BusinessException 토큰 유효하지 않음, 사원 없음
     */
    public SurveyIntroResult processIntroToken(String token) {

        // 1) Redis 토큰 조회
        String payload = surveyTokenRepository.find(token)
                .orElseThrow(() -> {
                    log.warn("설문 인트로 접근 거부: 유효하지 않거나 만료된 토큰 token={}", token);
                    return new BusinessException(ErrorCode.SURVEY_TOKEN_INVALID);
                });

        // 2) 단발성 보장 — 조회 즉시 폐기
        surveyTokenRepository.delete(token);

        // 3) payload 파싱 — "empNo:surveyId"
        String[] parts = payload.split(":");
        if (parts.length != 2) {
            log.error("설문 인트로 접근 거부: 잘못된 토큰 페이로드 형식 payload={}", payload);
            throw new BusinessException(ErrorCode.SURVEY_TOKEN_INVALID);
        }

        String empNo = parts[0];
        Long surveyId;
        try {
            surveyId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            log.error("설문 인트로 접근 거부: 설문 ID 파싱 실패 payload={}", payload);
            throw new BusinessException(ErrorCode.SURVEY_TOKEN_INVALID);
        }

        // 4) empNo → empId 변환
        Employee employee = employeeRepository.findByEmpNo(empNo)
                .orElseThrow(() -> {
                    log.warn("설문 인트로 접근 거부: 존재하지 않는 사원 empNo={}", empNo);
                    return new BusinessException(ErrorCode.SURVEY_NOT_TARGET);
                });

        // 5) Client JWT 발급 (쿠키 저장은 Controller 에서 수행)
        String clientToken = clientTokenProvider.generateClientToken(employee.getId());

        log.info("설문 인트로 접근 허용 + Client JWT 발급: empNo={}, empId={}, surveyId={}",
                empNo, employee.getId(), surveyId);

        return new SurveyIntroResult(clientToken, surveyId);
    }
}

