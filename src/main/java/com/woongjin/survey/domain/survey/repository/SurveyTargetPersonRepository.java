package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.SurveyTargetPerson;
import com.woongjin.survey.domain.survey.domain.SurveyTargetPersonId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyTargetPersonRepository extends JpaRepository<SurveyTargetPerson, SurveyTargetPersonId> {

    /**
     * 설문 대상자 여부 확인
     * - 복합 PK (SVY_ID, EMP_ID) 로 존재 여부 조회
     */
    boolean existsById(SurveyTargetPersonId id);
}
