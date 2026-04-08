package com.woongjin.survey.domain.employee.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 직원 재직 상태
 * - DB 저장값: name() 그대로 ("ACTIVE", "INACTIVE", "LEAVE")
 */
@Getter
@RequiredArgsConstructor
public enum EmployeeStatus {

    ACTIVE("재직"),
    INACTIVE("퇴직"),
    LEAVE("휴직");

    private final String description;
}
