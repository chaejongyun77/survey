package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.domain.SurveyQuestion;
import com.woongjin.survey.domain.survey.domain.SurveyQuestionItem;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.AnswerDto;
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
     * 설문 답변 전체 검증
     *
     * @param questions 해당 설문의 전체 문항 목록 (옵션 포함)
     * @param answers   제출된 답변 목록
     * @param strict    true = 필수 문항 누락 체크 (최종 제출), false = skip (임시저장)
     */
    public void validate(List<SurveyQuestion> questions, List<AnswerDto> answers, boolean strict) {

        // 빠른 조회를 위한 맵 구성
        Map<Long, SurveyQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        Map<Long, AnswerDto> answerMap = answers.stream()
                .collect(Collectors.toMap(AnswerDto::getQuestionId, a -> a));

        // 1) 모든 answer 의 questionId 가 설문의 문항인지
        validateQuestionIds(answerMap.keySet(), questionMap.keySet());

        // 2) 조건분기 활성화 기반 "응답해야 할 문항" 계산
        Set<Long> activeQuestionIds = calculateActiveQuestions(questions, answerMap);

        // 3) 필수 문항 누락 검증 (strict 모드에서만)
        if (strict) {
            validateRequired(questions, answerMap, activeQuestionIds);
        }

        // 4~5) 각 답변의 유형별 / 옵션 ID 검증
        for (AnswerDto answer : answers) {
            SurveyQuestion question = questionMap.get(answer.getQuestionId());
            validateAnswerByType(question, answer);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 1) questionId 유효성
    // ─────────────────────────────────────────────────────────────

    private void validateQuestionIds(Set<Long> answerQuestionIds, Set<Long> validQuestionIds) {
        for (Long qid : answerQuestionIds) {
            if (!validQuestionIds.contains(qid)) {
                log.warn("존재하지 않는 문항 ID: {}", qid);
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

    private Set<Long> calculateActiveQuestions(List<SurveyQuestion> questions,
                                               Map<Long, AnswerDto> answerMap) {
        // 역방향 인덱스: childQuestionId → 부모 문항 (O(N)에 한 번만 구축)
        // 이후 분기 문항마다 O(1) 조회 → 전체 O(N)
        Map<Long, SurveyQuestion> parentByChildId = questions.stream()
                .filter(q -> q.getChildQuestionId() != null)
                .collect(Collectors.toMap(
                        SurveyQuestion::getChildQuestionId,
                        q -> q
                ));

        Set<Long> active = new HashSet<>();

        for (SurveyQuestion q : questions) {
            // 분기 없는 문항은 항상 활성
            if (q.getParentItemId() == null) {
                active.add(q.getId());
                continue;
            }

            // 분기 문항은 부모 답변 확인
            SurveyQuestion parent = parentByChildId.get(q.getId());
            if (parent == null) continue;

            AnswerDto parentAnswer = answerMap.get(parent.getId());
            if (parentAnswer == null || parentAnswer.getSelectedItemIds() == null) continue;

            // 부모 답변에 parentItemId 가 포함되어 있으면 활성
            if (parentAnswer.getSelectedItemIds().contains(q.getParentItemId())) {
                active.add(q.getId());
            }
        }

        return active;
    }

    // ─────────────────────────────────────────────────────────────
    // 3) 필수 문항 누락 검증
    // ─────────────────────────────────────────────────────────────

    private void validateRequired(List<SurveyQuestion> questions,
                                  Map<Long, AnswerDto> answerMap,
                                  Set<Long> activeQuestionIds) {
        for (SurveyQuestion q : questions) {
            if (!Boolean.TRUE.equals(q.getRequired())) continue;   // 필수 아니면 skip
            if (!activeQuestionIds.contains(q.getId()))   continue; // 비활성 문항은 skip

            if (!answerMap.containsKey(q.getId())) {
                log.warn("필수 문항 누락: questionId={}, name={}", q.getId(), q.getQuestionName());
                throw new BusinessException(ErrorCode.ANSWER_REQUIRED_MISSING);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 4~5) 유형별 필드 조합 + 옵션 ID 검증
    // ─────────────────────────────────────────────────────────────

    private void validateAnswerByType(SurveyQuestion question, AnswerDto answer) {
        // 요청의 type 이 실제 문항 유형과 일치하는지
        if (question.getQuestionType() != answer.getType()) {
            log.warn("문항 유형 불일치: questionId={}, 실제={}, 요청={}",
                    question.getId(), question.getQuestionType(), answer.getType());
            throw new BusinessException(ErrorCode.ANSWER_TYPE_MISMATCH);
        }

        Set<Long> validOptionIds = question.getItems().stream()
                .map(SurveyQuestionItem::getId)
                .collect(Collectors.toSet());

        switch (question.getQuestionType()) {
            case SINGLE_CHOICE   -> validateSingleChoice(answer, validOptionIds);
            case MULTIPLE_CHOICE -> validateMultipleChoice(answer, validOptionIds);
            case SUBJECTIVE      -> validateSubjective(answer);
            case SCALE           -> validateScale(answer);
            case RANKING         -> validateRanking(answer, validOptionIds);
        }
    }

    private void validateSingleChoice(AnswerDto answer, Set<Long> validOptionIds) {
        List<Long> selected = answer.getSelectedItemIds();
        if (selected == null || selected.size() != 1) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        if (!validOptionIds.contains(selected.get(0))) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_OPTION);
        }
    }

    private void validateMultipleChoice(AnswerDto answer, Set<Long> validOptionIds) {
        List<Long> selected = answer.getSelectedItemIds();
        if (selected == null || selected.isEmpty()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 중복 선택 불가
        if (new HashSet<>(selected).size() != selected.size()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        for (Long itemId : selected) {
            if (!validOptionIds.contains(itemId)) {
                throw new BusinessException(ErrorCode.ANSWER_INVALID_OPTION);
            }
        }
    }

    private void validateSubjective(AnswerDto answer) {
        String text = answer.getTextAnswer();
        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 최대 길이 제약은 AnswerDto @Size 에서 이미 처리됨
    }

    private void validateScale(AnswerDto answer) {
        if (answer.getScaleValue() == null) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 척도 범위는 정책 확정 시 추가 검증
    }

    private void validateRanking(AnswerDto answer, Set<Long> validOptionIds) {
        List<Long> ranked = answer.getRankedItemIds();
        if (ranked == null || ranked.isEmpty()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 중복 없음
        if (new HashSet<>(ranked).size() != ranked.size()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        // 모든 옵션이 포함되어야 함 (부분 순위 금지)
        if (ranked.size() != validOptionIds.size()) {
            throw new BusinessException(ErrorCode.ANSWER_INVALID_FORMAT);
        }
        for (Long itemId : ranked) {
            if (!validOptionIds.contains(itemId)) {
                throw new BusinessException(ErrorCode.ANSWER_INVALID_OPTION);
            }
        }
    }
}
