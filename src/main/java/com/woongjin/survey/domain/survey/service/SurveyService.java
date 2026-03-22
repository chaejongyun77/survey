package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.dto.SurveyCreateRequestDto;
import com.woongjin.survey.domain.survey.dto.SurveyResponseDto;
import com.woongjin.survey.domain.survey.dto.SurveyUpdateRequestDto;

import java.util.List;

/**
 * 설문 서비스 인터페이스
 */
public interface SurveyService {

    // 전체 목록 조회
    List<SurveyResponseDto> getSurveyList();

    // 단건 조회
    SurveyResponseDto getSurvey(Long surveyId);

    // 설문 생성
    void createSurvey(SurveyCreateRequestDto requestDto, String createdBy);

    // 설문 수정
    void updateSurvey(Long surveyId, SurveyUpdateRequestDto requestDto);

    // 설문 삭제
    void deleteSurvey(Long surveyId);
}
