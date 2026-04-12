package com.woongjin.survey.domain.survey.domain;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설문 대상자 엔티티 - SVY_TRPSN_TB 테이블 매핑
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "svy_trpsn_tb")
public class SurveyTargetPerson extends BaseEntity {

    @EmbeddedId
    private SurveyTargetPersonId id;

    @MapsId("surveyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SVY_ID", nullable = false)
    private Survey survey;

    @MapsId("empId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_ID", nullable = false)
    private Employee employee;
}
