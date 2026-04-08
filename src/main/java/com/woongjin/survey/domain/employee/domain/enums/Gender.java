package com.woongjin.survey.domain.employee.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 성별
 * - DB 저장값: code ("M", "F") — name()이 아닌 code를 저장
 * - @Enumerated(EnumType.STRING) 대신 @Column + getter로 직접 매핑
 */
@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE("남성"),
    FEMALE("여성");

    // 화면(프론트엔드)에 "남성", "여성"으로 보여주기 위한 용도
    private final String description;
}