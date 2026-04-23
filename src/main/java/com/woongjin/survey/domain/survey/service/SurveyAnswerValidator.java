package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.domain.Question;
import com.woongjin.survey.domain.survey.domain.QuestionItem;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 설문 답변 비즈니스 검증기
 *
 * [검증 범위]
 * - 형식 검증(Bean Validation)은 AnswerDto 의 @NotNull / @Size 가 담당
 * - 이 클래스는 "설문 문항 정보와 교차 검증"이 필요한 로직을 수행
 *
 * [검증 순서]
 *  1) questionId 가 실제 설문의 문항인지
 *  2) 조건분기 활성화 기반 "유효 문항 목록" 계산
 *  3) 필수 문항 누락 여부
 *  4) 유형별 필드 조합 (SINGLE_CHOICE 는 1개 등)
 *  5) 옵션 ID 가 실제 문항의 옵션인지
 *
 * [설계 의도]
 * - DB 접근 없음 — 호출자가 SurveyQuestion 목록을 전달
 * - 순수 비즈니스 로직이라 단위 테스트 용이
 * - 최종 제출 / 임시 저장에서 재사용 가능 (required 옵션으로 필수 체크 토글)
 */
@Slf4j
@Component
public class SurveyAnswerValidator {

    /**
     * 설문 답변 전체 검증 (진입점)
     *
     * @param questions 해당 설문의 전체 문항 목록 (옵션 포함)
     * @param answers   제출된 답변 목록
     * @param strict    true = 필수 문항 누락 체크 (최종 제출), false = skip (임시저장)
     */
    public void validate(List<Question> questions, List<SurveyAnswerDto> answers, boolean strict) {

        // 빠른 조회를 위한 맵 구성 (questionId → 문항/답변)
        Map<Long, Question> questionById = questions.stream()
                .collect(Collectors.toMap(Question::getId, question -> question));

        Map<Long, SurveyAnswerDto> answerByQuestionId = answers.stream()
                .collect(Collectors.toMap(SurveyAnswerDto::getQuestionId, answer -> answer));

        // 1) 모든 answer 의 questionId 가 설문의 문항인지
        validateQuestionIds(answerByQuestionId.keySet(), questionById.keySet());

        // 2) 조건분기 활성화 기반 "응답해야 할 문항" 계산
        Set<Long> activeQuestionIds = calculateActiveQuestions(questions, answerByQuestionId);

        // 3) 필수 문항 누락 검증 (strict 모드에서만)
        if (strict) {
            validateRequired(questions, answerByQuestionId, activeQuestionIds);
        }

        // 4~5) 각 답변의 유형별 / 옵션 ID 검증
        for (SurveyAnswerDto answer : answers) {
            Question question = questionById.get(answer.getQuestionId());
            validateAnswerByType(question, answer);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 1) questionId 유효성
    // ─────────────────────────────────────────────────────────────

    /**
     * 제출된 답변의 questionId 가 모두 이 설문에 속한 문항인지 확인한다.
     *
     * @param answeredQuestionIds 제출된 답변들의 questionId 집합
     * @param surveyQuestionIds   해당 설문이 실제로 보유한 questionId 집합
     * @throws BusinessException ANSWER_INVALID_QUESTION — 외부 questionId 가 섞여있을 때
     */
    private void validateQuestionIds(Set<Long> answeredQuestionIds, Set<Long> surveyQuestionIds) {
        for (Long questionId : answeredQuestionIds) {
            if (!surveyQuestionIds.contains(questionId)) {
                log.warn("존재하지 않는 문항 ID: {}", questionId);
                throw new BusinessException(ErrorCode.ANSWER_INVALID_QUESTION);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2) 조건분기 활성 문항 계산
    //
    // 규칙:
    //  - 분기 없는 문항(parentItemId == null) : 항상 활성
    //  - 분기 있는 문항(parentItemId != null) : 부모 답변이 parentItemId 를 선택했을 때만 활성
    // ─────────────────────────────────────────────────────────────

    /**
     * 조건분기를 고려하여 "실제로 응답 대상이 되는 문항 ID 집합"을 계산한다.
     * 이 집합은 필수 문항 누락 검증에서 사용된다.
     *
     * @param questions          전체 문항 목록
     * @param answerByQuestionId questionId → 답변 맵
     * @return 활성 상태인 questionId 집합
     */
    private Set<Long> calculateActiveQuestions(List<Question> questions,
                                               Map<Long, SurveyAnswerDto> answerByQuestionId) {
        // 역방향 인덱스: 자식 questionId → 부모 문항 (O(N)에 한 번만 구축)
        // 이후 분기 문항마다 O(1) 조회 → 전체 O(N)
        Map<Long, Question> parentQuestionByChildId = questions.stream()
                .filter(question -> question.getChildQuestionId() != null)
                .collect(Collectors.toMap(
                        Question::getChildQuestionId,
                        question -> question
                ));

        Set<Long> activeQuestionIds = new HashSet<>();

        for (Question question : questions) {
            // 분기 없는 문항은 항상 활성
            if (question.getParentItemId() == null) {
                activeQuestionIds.add(question.getId());
                continue;
            }

            // 분기 문항은 부모 답변을 확인하여 활성 여부 결정
            Question parentQuestion = parentQuestionByChildId.get(question.getId());
            if (parentQuestion == null) continue;

            SurveyAnswerDto parentAnswer = answerByQuestionId.get(parentQuestion.getId());
            if (parentAnswer == null || parentAnswer.getSelectedItemIds() == null) continue;

            // 부모 답변에 이 문항의 parentItemId 가 포함되어 있으면 활성
            if (parentAnswer.getSelectedItemIds().contains(question.getParentItemId())) {
                activeQuestionIds.add(question.getId());
            }
        }

        return activeQuestionIds;
    }

    // ─────────────────────────────────────────────────────────────
    // 3) 필수 문항 누락 검증
    // ─────────────────────────────────────────────────────────────

    /**
     * 활성 상태이면서 필수인 문항 중 답변이 누락된 것이 있는지 확인한다.
     * strict=true (최종 제출) 일 때만 호출된다.
     *
     * @param questions          전체 문항 목록
     * @param answerByQuestionId questionId → 답변 맵
     * @param activeQuestionIds  조건분기 계산 결과 활성 문항 ID 집합
     * @throws BusinessException ANSWER_REQUIRED_MISSING — 필수 문항 답변이 없을 때
     */
    private void validateRequired(List<Question> questions,
                                  Map<Long, SurveyAnswerDto> answerByQuestionId,
                                  Set<Long> activeQuestionIds) {
        for (Question question : questions) {
            if (!Boolean.TRUE.equals(question.getRequired())) continue;   // 필수 아니면 skip
            if (!activeQuestionIds.contains(question.getId())) continue;  // 비활성 문항은 skip

            if (!answerByQuestionId.containsKey(question.getId())) {
                log.warn("필수 문항 누락: questionId={}, name={}", question.getId(), question.getQuestionName());
                throw new BusinessException(ErrorCode.ANSWER_REQUIRED_MISSING);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 4~5) 유형별 필드 조합 + 옵션 ID 검증
    // ─────────────────────────────────────────────────────────────

    /**
     * 단일 답변에 대해 문항 유형별 검증을 수행한다.
     *  - 요청의 type 이 실제 문항 유형과 일치하는지
     *  - 유형별 세부 규칙 (SINGLE_CHOICE 는 1개 선택, RANKING 은 모든 옵션 포함 등)
     *
     * @param question 답변이 속한 문항
     * @param answer   검증 대상 답변
     */
    private void validateAnswerByType(Question question, SurveyAnswerDto answer) {
        if (question.getQuestionType() != answer.getType()) {
            log.warn("문항 유형 불일치: questionId={}, 실제={}, 요청={}",
                    question.getId(), question.getQuestionType(), answer.getType());
            throw new BusinessException(ErrorCode.ANSWER_TYPE_MISMATCH);
        }

        Set<Long> validOptionIds = question.getItems().stream()
                .map(QuestionItem::getId)
                .collect(Collectors.toSet());

        switch (question.getQuestionType()) {
            case SINGLE_CHOICE   -> validateSingleChoice(answer, validOptionIds);
            case MULTIPLE_CHOICE -> validateMultipleChoice(answer, validOptionIds);
            case SUBJECTIVE      -> validateSubjective(answer);
            case SCALE           -> validateScale(answer);
            case RANKING         -> validateRanking(answer, validOptionIds);
        }
    }

    /**
     * 단일 선택: 선택 항목이 정확히 1개이고, 옵션 ID 가 유효해야 한다.
     */
    private void validateSingleChoice(SurveyAnswerDto answer, Set<Long> validOptionIds) {
        List<Long> selectedItemIds = answer.getSelectedItemIds();
        if (selectedItemIds == null || selectedItemIds.size() != 1) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        if (!validOptionIds.contains(selectedItemIds.get(0))) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_OPTION);
        }
    }

    /**
     * 복수 선택: 1개 이상 선택, 중복 선택 불가, 모든 옵션 ID 가 유효해야 한다.
     */
    private void validateMultipleChoice(SurveyAnswerDto answer, Set<Long> validOptionIds) {
        List<Long> selectedItemIds = answer.getSelectedItemIds();
        if (selectedItemIds == null || selectedItemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 중복 선택 불가
        if (new HashSet<>(selectedItemIds).size() != selectedItemIds.size()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        for (Long itemId : selectedItemIds) {
            if (!validOptionIds.contains(itemId)) {
                throw new BusinessException(ErrorCode.ANSWER_INVALID_OPTION);
            }
        }
    }

    /**
     * 주관식: 공백이 아닌 텍스트가 존재해야 한다.
     * (최대 길이는 AnswerDto @Size 에서 처리됨)
     */
    private void validateSubjective(SurveyAnswerDto answer) {
        String textAnswer = answer.getTextAnswer();
        if (textAnswer == null || textAnswer.isBlank()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
    }

    /**
     * 척도: scaleValue 가 존재해야 한다.
     * (척도 범위 검증은 정책 확정 시 추가)
     */
    private void validateScale(SurveyAnswerDto answer) {
        if (answer.getScaleValue() == null) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
    }

    /**
     * 순위 선택: 모든 옵션이 중복 없이 포함되어야 하며, 부분 순위는 금지된다.
     */
    private void validateRanking(SurveyAnswerDto answer, Set<Long> validOptionIds) {
        List<Long> rankedItemIds = answer.getRankedItemIds();
        if (rankedItemIds == null || rankedItemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 중복 없음
        if (new HashSet<>(rankedItemIds).size() != rankedItemIds.size()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 모든 옵션이 포함되어야 함 (부분 순위 금지)
        if (rankedItemIds.size() != validOptionIds.size()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        for (Long itemId : rankedItemIds) {
            if (!validOptionIds.contains(itemId)) {
                throw new BusinessException(ErrorCode.ANSWER_INVALID_OPTION);
            }
        }
    }
}
