package com.woongjin.survey.domain.survey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 설문 대상자 복합 PK - (SVY_ID, EMP_ID)
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SurveyTargetPersonId implements Serializable {

    @Column(name = "SVY_ID")
    private Long surveyId;

    @Column(name = "EMP_ID")
    private Long empId;
}
