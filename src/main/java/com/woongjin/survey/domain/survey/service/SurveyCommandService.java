package com.woongjin.survey.domain.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.survey.domain.Answer;
import com.woongjin.survey.domain.survey.domain.Question;
import com.woongjin.survey.domain.survey.domain.QuestionBranch;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResult;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import com.woongjin.survey.domain.survey.dto.submit.SubmitRequest;
import com.woongjin.survey.domain.survey.infra.SurveyDraftRepository;
import com.woongjin.survey.domain.survey.infra.SurveyTokenRepository;
import com.woongjin.survey.domain.survey.repository.QuestionBranchRepository;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyResponseRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 설문 커맨드 서비스
 * - 8081 요청 시 empNo 기반으로 진행중 설문 조회 + Redis 토큰 발급 (issue)
 * - 설문 인트로 진입 시 Redis 토큰 검증 + Client JWT 발급 (processIntroToken)
 * - 임시저장 저장/삭제 (saveDraft, deleteDraft)
 * - 설문 최종 제출 (submit)
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
    private final SurveyQuestionRepository     surveyQuestionRepository;
    private final QuestionBranchRepository     questionBranchRepository;
    private final SurveyResponseRepository     surveyResponseRepository;
    private final SurveyAnswerValidator        answerValidator;
    private final SurveyDraftRepository        surveyDraftRepository;
    private final ObjectMapper                 objectMapper;

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
        Long surveyId = surveyQueryService.findActiveSurveyIdByEmpId(employee.getId())
                .orElseThrow(() -> {
                    log.debug("설문 토큰 발급 거부: 진행중인 설문 없음 empNo={}, empId={}", empNo, employee.getId());
                    return new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
                });

        // 3) 이미 응답 완료 여부 확인
        if (participationValidator.hasAlreadySubmitted(surveyId, employee.getId())) {
            log.debug("설문 토큰 발급 거부: 이미 응답 완료 empNo={}, surveyId={}", empNo, surveyId);
            throw new BusinessException(ErrorCode.SURVEY_ALREADY_DONE);
        }

        // 4) Redis 토큰 발급
        String token = surveyTokenRepository.save(empNo, surveyId);
        log.info("설문 토큰 발급 완료: empNo={}, surveyId={}", empNo, surveyId);

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

    // ─────────────────────────────────────────────────────────────
    // 임시저장 — 저장 / 삭제
    // ─────────────────────────────────────────────────────────────

    /**
     * 임시저장
     * - 참여 가능 여부(기간·대상자·이미 제출) 검증 후 Redis 에 저장
     * - 빈 answers 허용 (부분 저장 지원)
     */
    public void saveDraft(Long surveyId, Long empId, SubmitRequest request) {
        participationValidator.checkParticipate(surveyId, empId);
        List<SurveyAnswerDto> answers = request.getAnswers() != null ? request.getAnswers() : List.of();
        surveyDraftRepository.save(empId, surveyId, answers);
        log.info("[saveDraft] 완료: surveyId={}, empId={}, answers.size={}", surveyId, empId, answers.size());
    }

    /**
     * 임시저장 삭제 (최종 제출 후 / 인트로에서 "새로 시작" 선택 시 호출)
     */
    public void deleteDraft(Long surveyId, Long empId) {
        surveyDraftRepository.delete(empId, surveyId);
        log.info("[deleteDraft] 임시저장 삭제: surveyId={}, empId={}", surveyId, empId);
    }

    // ─────────────────────────────────────────────────────────────
    // 설문 최종 제출
    //
    // [처리 순서]
    //  1) 참여 가능 여부 재검증 (TOCTOU 방지 / 직접 호출 방지)
    //  2) 문항 목록 조회 (옵션 fetch join 포함)
    //  3) 답변 비즈니스 검증 (SurveyAnswerValidator, strict=true)
    //  4) answers 리스트 → JSON 직렬화
    //  5) SurveyResponse INSERT
    //  6) Redis 임시저장 데이터 삭제
    //
    // [설계 포인트]
    // - empId 는 Client JWT 클레임에서 이미 확보됨 → Employee 재조회 불필요
    // - AuditorAwareImpl 이 request attribute(clientEmpId) 에서 empId 를 읽으므로
    //   SecurityContext 조작 불필요
    // ─────────────────────────────────────────────────────────────

    /**
     * 설문 최종 제출
     *
     * @param surveyId 설문 ID
     * @param empId    사원 PK (ClientTokenFilter 에서 주입)
     * @param request  제출 요청 바디
     */
    @Transactional
    public void submit(Long surveyId, Long empId, SubmitRequest request) {

        log.info("[submit] 시작: surveyId={}, empId={}, answers.size={}",
                surveyId, empId, request.getAnswers().size());

        // ① 참여 가능 여부 재검증
        participationValidator.checkParticipate(surveyId, empId);
        log.info("[submit] ① 참여 가능 검증 통과");

        // ② 문항 목록 조회 (옵션 포함)
        List<Question> questions = surveyQuestionRepository.findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(surveyId);
        log.info("[submit] ② 문항 조회 완료: questions.size={}", questions.size());

        // ②-1 분기 정보 조회
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        List<QuestionBranch> branches = questionIds.isEmpty()
                ? List.of()
                : questionBranchRepository.findByParentQuestionIdIn(questionIds);
        log.info("[submit] ②-1 분기 조회 완료: branches.size={}", branches.size());

        // ③ 답변 비즈니스 검증 (최종 제출이므로 strict=true)
        List<SurveyAnswerDto> answers = request.getAnswers();
        answerValidator.validate(questions, branches, answers, true);
        log.info("[submit] ③ 답변 검증 통과");

        // ④ JSON 직렬화

        // ⑤ DB INSERT
        //    - AuditorAwareImpl 이 request attribute(clientEmpId) 에서 empId 를 읽어
        //      FRST_CRTN_ID / RCNT_UPDT_ID 자동 세팅 (SecurityContext 조작 불필요)
        Answer response = Answer.builder()
                .surveyId(surveyId)
                .empId(empId)
                .answers(answers)
                .build();

        surveyResponseRepository.save(response);
        log.info("[submit] ⑤ 설문 제출 완료: surveyId={}, empId={}", surveyId, empId);

        // ⑥ Redis 임시저장 삭제
        surveyDraftRepository.delete(empId, surveyId);
        log.info("[submit] ⑥ 임시저장 삭제 완료: surveyId={}, empId={}", surveyId, empId);
    }

}

