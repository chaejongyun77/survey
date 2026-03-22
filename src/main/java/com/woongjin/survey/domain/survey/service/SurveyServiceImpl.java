package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.dto.SurveyCreateRequestDto;
import com.woongjin.survey.domain.survey.dto.SurveyResponseDto;
import com.woongjin.survey.domain.survey.dto.SurveyUpdateRequestDto;
import com.woongjin.survey.domain.survey.entity.Survey;
import com.woongjin.survey.domain.survey.repository.SurveyMapper;
import com.woongjin.survey.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final SurveyMapper surveyMapper;

    @Override
    public List<SurveyResponseDto> getSurveyList() {
        return surveyMapper.findAll()
                .stream()
                .map(SurveyResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public SurveyResponseDto getSurvey(Long surveyId) {
        Survey survey = surveyMapper.findById(surveyId)
                .orElseThrow(() -> new CustomException("설문을 찾을 수 없습니다. id=" + surveyId));
        return SurveyResponseDto.from(survey);
    }

    @Override
    public void createSurvey(SurveyCreateRequestDto requestDto, String createdBy) {
        Survey survey = Survey.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .status("DRAFT")
                .createdBy(createdBy)
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .build();
        surveyMapper.insert(survey);
    }

    @Override
    public void updateSurvey(Long surveyId, SurveyUpdateRequestDto requestDto) {
        surveyMapper.findById(surveyId)
                .orElseThrow(() -> new CustomException("설문을 찾을 수 없습니다. id=" + surveyId));

        Survey survey = Survey.builder()
                .surveyId(surveyId)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .status(requestDto.getStatus())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .build();
        surveyMapper.update(survey);
    }

    @Override
    public void deleteSurvey(Long surveyId) {
        surveyMapper.findById(surveyId)
                .orElseThrow(() -> new CustomException("설문을 찾을 수 없습니다. id=" + surveyId));
        surveyMapper.deleteById(surveyId);
    }
}
