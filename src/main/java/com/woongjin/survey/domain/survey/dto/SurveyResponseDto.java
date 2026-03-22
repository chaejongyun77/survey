package com.woongjin.survey.domain.survey.dto;

import com.woongjin.survey.domain.survey.entity.Survey;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 설문 응답 DTO - Entity → DTO 변환
 */
@Getter
@Builder
public class SurveyResponseDto {

    private Long surveyId;
    private String title;
    private String description;
    private String status;
    private String createdBy;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity → ResponseDto 변환 팩토리 메서드
     */
    public static SurveyResponseDto from(Survey survey) {
        return SurveyResponseDto.builder()
                .surveyId(survey.getSurveyId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .status(survey.getStatus())
                .createdBy(survey.getCreatedBy())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .build();
    }
}
