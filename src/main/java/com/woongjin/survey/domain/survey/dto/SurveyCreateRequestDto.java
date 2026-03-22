package com.woongjin.survey.domain.survey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설문 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SurveyCreateRequestDto {

    @NotBlank(message = "설문 제목은 필수입니다.")
    private String title;

    private String description;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;
}
