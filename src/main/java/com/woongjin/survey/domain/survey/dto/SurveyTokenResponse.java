package com.woongjin.survey.domain.survey.dto;

import lombok.Getter;

/**
 * 8081 → 8080 설문 체크 API 응답 DTO
 *
 * 설문이 있을 때: { "hasSurvey": true,  "token": "survey_tk_9a8b7c..." }
 * 설문이 없을 때: { "hasSurvey": false, "token": null }
 */
@Getter
public class SurveyTokenResponse {

    private final boolean hasSurvey;
    private final String token;

    private SurveyTokenResponse(boolean hasSurvey, String token) {
        this.hasSurvey = hasSurvey;
        this.token = token;
    }

    public static SurveyTokenResponse of(String token) {
        return new SurveyTokenResponse(true, token);
    }

    public static SurveyTokenResponse noSurvey() {
        return new SurveyTokenResponse(false, null);
    }
}
