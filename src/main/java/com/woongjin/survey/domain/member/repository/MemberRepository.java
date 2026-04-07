package com.woongjin.survey.domain.member.repository;

import com.woongjin.survey.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 JPA Repository
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 ID + ACTIVE 상태로 조회 (Spring Security 인증에서 사용)
    Optional<Member> findByLoginIdAndStatus(String loginId, String status);

    // PK + ACTIVE 상태로 조회 (토큰 재발급 시 사용)
    Optional<Member> findByMemberIdAndStatus(Long memberId, String status);
}
