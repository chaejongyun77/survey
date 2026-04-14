package com.woongjin.survey.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설문 인트로 화면용 DTO
 * - 제목, 기간, 대상자 수
 *
 * inProgress 필드 제거:
 * findActiveByEmpId / findIntroById 모두 WHERE 절에서 진행중 조건을 걸므로
 * 조회 결과가 존재하면 항상 진행중 → 별도 boolean 불필요
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

    /** 설문 대상자 수 (svy_trpsn_tb COUNT) */
    private long targetCount;
}
