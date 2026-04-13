package com.woongjin.survey.domain.survey.domain;

import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설문 응답 엔티티 - SVY_RSPN_TB 테이블 매핑
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "svy_rspn_tb",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_survey_response_survey_emp",
                columnNames = {"SVY_ID", "EMP_ID"}
        )
)
public class SurveyResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RSPN_ID")
    private Long id;

    @Column(name = "SVY_ID", nullable = false)
    private Long surveyId;

    @Column(name = "EMP_ID", nullable = false)
    private Long empId;

    /**
     * 문항 답변 JSON
     */
    @Column(name = "QST_ANSWR", nullable = false, columnDefinition = "JSON")
    private String answers;
}
