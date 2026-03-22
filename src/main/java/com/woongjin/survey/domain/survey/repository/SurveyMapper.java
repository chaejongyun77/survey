package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.entity.Survey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 설문 MyBatis Mapper 인터페이스
 * SQL은 resources/mapper/SurveyMapper.xml 에서 관리
 */
@Mapper
public interface SurveyMapper {

    // 목록 조회
    List<Survey> findAll();

    // 단건 조회
    Optional<Survey> findById(Long surveyId);

    // 등록
    void insert(Survey survey);

    // 수정
    void update(Survey survey);

    // 삭제
    void deleteById(Long surveyId);
}
