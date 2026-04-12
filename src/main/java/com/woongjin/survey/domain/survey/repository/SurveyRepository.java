package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long>, SurveyRepositoryCustom {
}
