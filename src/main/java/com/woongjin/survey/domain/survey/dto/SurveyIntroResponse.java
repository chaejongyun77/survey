package com.woongjin.survey.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설문 인트로 화면 API 응답 DTO
 * - 조회 결과가 존재하면 항상 진행중이므로 status 필드 없음
 * - 프론트에서 beginDate/endDate를 직접 조합하여 표시
 */
@Getter
@AllArgsConstructor
public class SurveyIntroResponse {

    private Long surveyId;

    /** 설문 제목 (SVY_SJ) */
    private String title;

    /** 시작 일시 (BGN_DT) */
    private LocalDateTime beginDate;

    /** 종료 일시 (END_DT) */
    private LocalDateTime endDate;

    /** 설문 대상자 수 (svy_trpsn_tb COUNT) */
    private long targetCount;

    /** 설문 문항 수 (svy_qst_tb COUNT, 미삭제 기준) */
    private long questionCount;
}

