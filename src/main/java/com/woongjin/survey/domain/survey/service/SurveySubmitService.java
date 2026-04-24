package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.domain.Answer;
import com.woongjin.survey.domain.survey.domain.SurveyQuestion;
import com.woongjin.survey.domain.survey.dto.submit.AnswerDto;
import com.woongjin.survey.domain.survey.dto.submit.SubmitRequest;
import com.woongjin.survey.domain.survey.infra.SurveyDraftRepository;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 설문 제출 서비스
 *
 * [처리 순서]
 *  1) 참여 가능 여부 재검증 (TOCTOU 방지 / 직접 호출 방지)
 *  2) 문항 목록 조회 (옵션 fetch join 포함)
 *  3) 답변 비즈니스 검증 (SurveyAnswerValidator)
 *  4) SurveyResponse INSERT (Hibernate 6 JSON 매핑으로 직렬화 자동 처리)
 *  5) Redis 임시저장 삭제
 *
 * [설계 포인트]
 * - empId 는 Client JWT 클레임에서 이미 확보됨 → Employee 재조회 불필요
 * - AuditorAwareImpl 이 request attribute(clientEmpId) 에서 empId 를 읽으므로
 *   SecurityContext 조작 불필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveySubmitService {

    private final SurveyQuestionRepository     surveyQuestionRepository;
    private final SurveyResponseRepository     surveyResponseRepository;
    private final SurveyParticipationValidator participationValidator;
    private final SurveyAnswerValidator        answerValidator;
    private final SurveyDraftRepository        surveyDraftRepository;

    @Transactional
    public void submit(Long surveyId, Long empId, SubmitRequest request) {

        log.info("[submit] 시작: surveyId={}, empId={}, answers.size={}",
                surveyId, empId, request.getAnswers().size());

        // ① 참여 가능 여부 재검증
        participationValidator.checkParticipate(surveyId, empId);
        log.info("[submit] ① 참여 가능 검증 통과");

        // ② 문항 목록 조회 (옵션 포함)
        List<SurveyQuestion> questions = surveyQuestionRepository
                .findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(surveyId);
        log.info("[submit] ② 문항 조회 완료: questions.size={}", questions.size());

        // ③ 답변 비즈니스 검증 (최종 제출이므로 strict=true)
        List<AnswerDto> answers = request.getAnswers();
        answerValidator.validate(questions, answers, true);
        log.info("[submit] ③ 답변 검증 통과");

        // ④ DB INSERT — @JdbcTypeCode(SqlTypes.JSON) 이 Jackson으로 자동 직렬화
        Answer response = Answer.builder()
                .surveyId(surveyId)
                .empId(empId)
                .answers(answers)
                .build();

        surveyResponseRepository.save(response);
        log.info("[submit] ④ 설문 제출 완료: surveyId={}, empId={}", surveyId, empId);

        // ⑤ Redis 임시저장 삭제
        surveyDraftRepository.delete(empId, surveyId);
        log.info("[submit] ⑤ 임시저장 삭제 완료: surveyId={}, empId={}", surveyId, empId);
    }
}
