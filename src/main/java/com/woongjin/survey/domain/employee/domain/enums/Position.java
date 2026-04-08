package com.woongjin.survey.domain.employee.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 직급
 * - DB 저장값: name() 그대로 ("STAFF", "SENIOR", ...)
 * - order 값으로 직급 간 비교/정렬 가능
 */
@Getter
@RequiredArgsConstructor
public enum Position {

    STAFF(1, "인턴"),
    SENIOR(2, "팀장");


    private final int order;
    private final String description;

}
