package com.woongjin.survey.domain.survey.dto.submit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 설문 답변 1건
 *
 * [역할]
 * - API 요청 바디 (SubmitRequest.answers 의 원소)
 * - DB QST_ANSWR JSON 내부 원소
 * → 같은 구조라 하나의 DTO 로 통합
 *
 * [검증 수준]
 * - 여기서는 "형식적 필수값" 만 검증 (questionId, type, textAnswer 길이)
 * - 필수 문항 누락 / 유형별 필드 조합 / 조건분기 정합성 등은
 *   SurveyAnswerValidator 에서 DB 조회 기반 비즈니스 검증으로 수행
 *
 * [유형별 사용 필드]
 *  SINGLE_CHOICE   : selectedItemIds (원소 1개)
 *  MULTIPLE_CHOICE : selectedItemIds (원소 1개 이상)
 *  SUBJECTIVE      : textAnswer
 *  SCALE           : scaleValue
 *  RANKING         : rankedItemIds (순서가 곧 순위)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // null 필드는 JSON 에서 제외 (DB 저장 용량 최소화)
public class AnswerDto {

    /** 문항 ID (QST_ID) */
    @NotNull(message = "문항 ID는 필수입니다.")
    private Long questionId;

    /** 문항 유형 — 통계 쿼리에서 분기 기준 */
    @NotNull(message = "문항 유형은 필수입니다.")
    private QuestionType type;

    /**
     * 선택한 항목 ID 목록
     * - SINGLE_CHOICE / MULTIPLE_CHOICE 에서만 사용
     * - SINGLE_CHOICE 도 배열로 통일 (JSON_CONTAINS 쿼리 일관성)
     */
    private List<Long> selectedItemIds;

    /**
     * 주관식 답변 텍스트
     * - SUBJECTIVE 에서만 사용
     */
    @Size(max = 500, message = "주관식 답변은 500자를 초과할 수 없습니다.")
    private String textAnswer;

    /**
     * 척도 값
     * - SCALE 에서만 사용
     */
    private Integer scaleValue;

    /**
     * 순위 정렬된 항목 ID 목록
     * - RANKING 에서만 사용
     * - 배열의 순서가 곧 순위 (index 0 = 1순위)
     */
    private List<Long> rankedItemIds;
}

