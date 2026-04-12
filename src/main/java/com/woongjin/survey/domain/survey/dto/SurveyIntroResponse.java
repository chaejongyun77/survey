package com.woongjin.survey.domain.survey.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 설문 인트로 화면 API 응답 DTO
 * - SurveyIntroDto(내부 조회용)를 클라이언트 응답 형태로 변환
 */
@Getter
public class SurveyIntroResponse {

    private final Long surveyId;

    /** 설문 제목 */
    private final String title;

    /** 설문 기간 표시용 문자열 (예: 2026.03.18 ~ 2026.03.25) */
    private final String period;

    /** 진행 상태 라벨 (진행중 / 종료) */
    private final String status;

    /** 진행 여부 */
    private final boolean inProgress;

    /** 설문 대상자 수 */
    private final long targetCount;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private SurveyIntroResponse(SurveyIntroDto dto) {
        this.surveyId    = dto.getSurveyId();
        this.title       = dto.getTitle();
        this.period      = dto.getBeginDate().format(FORMATTER) + " ~ " + dto.getEndDate().format(FORMATTER);
        this.inProgress  = dto.isInProgress();
        this.status      = dto.isInProgress() ? "진행중" : "종료";
        this.targetCount = dto.getTargetCount();
    }

    public static SurveyIntroResponse from(SurveyIntroDto dto) {
        return new SurveyIntroResponse(dto);
    }
}
