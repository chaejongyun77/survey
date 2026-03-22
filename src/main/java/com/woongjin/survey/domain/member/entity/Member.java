package com.woongjin.survey.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원 Entity - MEMBER 테이블 매핑
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    private Long memberId;          // PK
    private String loginId;         // 로그인 ID (사원번호 등)
    private String password;        // BCrypt 암호화된 비밀번호
    private String name;            // 이름
    private String email;           // 이메일
    private String role;            // 권한: ROLE_USER / ROLE_ADMIN
    private String status;          // 상태: ACTIVE / INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
