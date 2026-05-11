/*
package com.woongjin.survey.domain.noti;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationHistoryListQueryResponse(
        Long surveyId,
        String surveySubject,
        NotificationType notificationType,
        LocalDateTime sendDate,
        Integer sendCount
) {

    public static NotificationHistoryListQueryResponse of(NotificationHistory history, SurveySubjectProjection surveySubject) {
        return NotificationHistoryListQueryResponse.builder()
                .surveyId(surveySubject.surveyId())
                .surveySubject(surveySubject.subject())
                .notificationType(history.getNotificationType())
                .sendDate(history.getCreatedDate())
                .sendCount(history.getSendCount())
                .build();
    }
}*/
