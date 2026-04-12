package com.woongjin.survey.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설문 인트로 화면용 DTO
 * - 제목, 기간, 진행상태, 대상자 수
 */
@Getter
@AllArgsConstructor
public class SurveyIntroDto {

    private Long surveyId;

    /** 설문 제목 (SVY_SJ) */
    private String title;

    /** 시작 일시 (BGN_DT) */
    private LocalDateTime beginDate;

    /** 종료 일시 (END_DT) */
    private LocalDateTime endDate;

    /** 진행 상태 - 현재 날짜가 BGN_DT ~ END_DT 사이면 true */
    private boolean inProgress;

    /** 설문 대상자 수 (svy_trpsn_tb COUNT) */
    private long targetCount;
}
