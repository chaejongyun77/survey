package com.woongjin.survey.domain.noti;

@Getter
@Builder
public class NotificationRequest {

    @NotNull
    private final NotificationType notificationType;

    @NotNull
    @Size(min = 1)
    private final List<Long> employeeIds;

    @NotNull
    private final Long referenceId;

    private final Map<String, Object> variables;
}