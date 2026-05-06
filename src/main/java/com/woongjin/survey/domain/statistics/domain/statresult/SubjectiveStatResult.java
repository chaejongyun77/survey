package com.woongjin.survey.domain.statistics.domain.statresult;

/**
 * 주관식 집계 결과.
 *
 * [의미]
 *  answeredCount: 비어있지 않은 텍스트로 응답한 사람 수
 *
 * [예시]
 *  { "answeredCount": 521 }
 *
 * [설계 메모]
 *  주관식 텍스트 자체는 통계 테이블에 저장하지 않는다.
 *  화면에서 "응답 내용 보기"를 누르면 그때 SVY_RSPN_TB 에서
 *  페이징 조회로 가져온다.
 */
public record SubjectiveStatResult(
        int answeredCount
) implements QuestionStatResult {
}
