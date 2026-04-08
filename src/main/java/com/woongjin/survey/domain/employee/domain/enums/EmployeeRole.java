package com.woongjin.survey.domain.employee.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 직원 권한
 * - DB 저장값: name() 그대로 ("ROLE_USER", "ROLE_ADMIN")
 */
@Getter
@RequiredArgsConstructor
public enum EmployeeRole {

    USER("일반 사용자"),
    ADMIN("일반 관리자"),
    SUPER_ADMIN("최고 관리자");

    private final String description;
}
