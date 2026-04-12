package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.SurveyTargetPerson;
import com.woongjin.survey.domain.survey.domain.SurveyTargetPersonId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyTargetPersonRepository extends JpaRepository<SurveyTargetPerson, SurveyTargetPersonId> {
}
