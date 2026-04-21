package com.woongjin.survey.domain.survey.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설문 인트로 처리 결과
 *
 * [사용처]
 * SurveyCommandService.processIntroToken() 의 반환값
 * - Controller 가 이 객체를 받아 쿠키 발급 + 뷰 렌더링 수행
 *
 * [설계 이유]
 * Service 가 HttpServletResponse 를 직접 받으면 Web 계층에 종속됨.
 * 대신 필요한 데이터(clientToken, surveyId)를 담아 반환하고,
 * 쿠키를 심는 행위는 Controller 에서 처리한다.
 */
@Getter
@RequiredArgsConstructor
public class SurveyIntroResult {

    /** 발급된 Client JWT — Controller 가 Set-Cookie 에 담아 응답 */
    private final String clientToken;

    /** 설문 ID — Controller 가 Model 에 담아 뷰로 전달 */
    private final Long surveyId;
}
