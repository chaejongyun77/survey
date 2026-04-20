package com.woongjin.survey.domain.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.survey.domain.SurveyQuestion;
import com.woongjin.survey.domain.survey.domain.SurveyResponse;
import com.woongjin.survey.domain.survey.dto.submit.AnswerDto;
import com.woongjin.survey.domain.survey.dto.submit.SubmitRequest;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyResponseRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 설문 제출 서비스
 *
 * [처리 순서]
 *  1) 참여 가능 여부 재검증 (TOCTOU 방지 / 직접 호출 방지)
 *  2) 문항 목록 조회 (옵션 fetch join 포함)
 *  3) 답변 비즈니스 검증 (SurveyAnswerValidator)
 *  4) answers 리스트 → JSON 직렬화
 *  5) SurveyResponse INSERT
 *  6) [TODO] Redis 임시저장 데이터 삭제
 *
 * [설계 포인트]
 * - empId 는 Client JWT 클레임에서 이미 확보됨 → Employee 재조회 불필요
 * - AuditorAware 가 읽을 수 있도록 SecurityContext 에 empId 기반 인증 정보를 임시 주입
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveySubmitService {

    private final EmployeeRepository          employeeRepository;
    private final SurveyQuestionRepository    surveyQuestionRepository;
    private final SurveyResponseRepository    surveyResponseRepository;
    private final SurveyQueryService          surveyQueryService;
    private final SurveyAnswerValidator       answerValidator;
    private final ObjectMapper                objectMapper;

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
        surveyQueryService.checkParticipate(surveyId, empId);
        log.info("[submit] ① 참여 가능 검증 통과");

        // ② 문항 목록 조회 (옵션 포함)
        List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyIdWithItems(surveyId);
        log.info("[submit] ② 문항 조회 완료: questions.size={}", questions.size());

        // ③ 답변 비즈니스 검증 (최종 제출이므로 strict=true)
        List<AnswerDto> answers = request.getAnswers();
        answerValidator.validate(questions, answers, true);
        log.info("[submit] ③ 답변 검증 통과");

        // ④ JSON 직렬화
        String answersJson = serializeAnswers(answers);

        // ⑤ DB INSERT
        //    - AuditorAware 가 SecurityContext 에서 empId 를 읽어 FRST_CRTN_ID / RCNT_UPDT_ID 를 자동 세팅
        //    - 설문 참여자는 Spring Security 로 인증되지 않으므로 임시로 SecurityContext 주입
        //    - finally 에서 반드시 정리 (세션 누수 방지)
        Authentication previous = SecurityContextHolder.getContext().getAuthentication();
        try {
            setClientAuditor(empId);

            SurveyResponse response = SurveyResponse.builder()
                    .surveyId(surveyId)
                    .empId(empId)
                    .answers(answersJson)
                    .build();

            surveyResponseRepository.save(response);
            log.info("[submit] ⑤ 설문 제출 완료: surveyId={}, empId={}", surveyId, empId);

        } finally {
            // 원래 인증 정보로 복구 (없었으면 clear)
            if (previous != null) {
                SecurityContextHolder.getContext().setAuthentication(previous);
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        // ⑥ TODO: Redis 임시저장 삭제
        // redisTemplate.delete("survey:temp:" + empId + ":" + surveyId);
    }

    /**
     * AuditorAware 가 읽을 수 있도록 SecurityContext 에 UserPrincipal 기반 인증 정보를 주입
     * - 설문 참여자 전용 임시 인증 — 실제 직원 로그인과는 무관
     * - Employee 를 여기서 조회 (UserPrincipal 구성에 empNo/empName 필요)
     */
    private void setClientAuditor(Long empId) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_TARGET));

        UserPrincipal principal = new UserPrincipal(
                employee.getId(),
                employee.getEmpNo(),
                "",
                employee.getEmpName(),
                Collections.emptyList()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ─────────────────────────────────────────────────────────────
    // private
    // ─────────────────────────────────────────────────────────────

    private String serializeAnswers(List<AnswerDto> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            log.error("답변 JSON 직렬화 실패", e);
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
    }
}
