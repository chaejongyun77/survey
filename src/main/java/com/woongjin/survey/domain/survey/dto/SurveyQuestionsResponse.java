package com.woongjin.survey.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 설문 문항 + 분기 정보 응답 DTO
 *
 * 프론트는 questions 와 branches 를 별도로 받아
 * 가시성 판단/렌더링 순서 계산에 사용한다.
 */
@Getter
@AllArgsConstructor
public class SurveyQuestionsResponse {

    /** 문항 목록 (정렬 순서 오름차순) */
    private final List<QuestionDto> questions;

    /** 분기 정의 목록 (분기 없으면 빈 리스트) */
    private final List<BranchDto> branches;
}
