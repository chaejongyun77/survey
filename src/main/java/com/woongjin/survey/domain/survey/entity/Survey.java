package com.woongjin.survey.domain.survey.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설문 Entity - DB 테이블 매핑 객체
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Survey {

    private Long surveyId;          // 설문 ID (PK)
    private String title;           // 설문 제목
    private String description;     // 설문 설명
    private String status;          // 상태: DRAFT / ACTIVE / CLOSED
    private String createdBy;       // 생성자 (사원번호)
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
