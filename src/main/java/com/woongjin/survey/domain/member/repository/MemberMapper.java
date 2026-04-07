package com.woongjin.survey.domain.member.repository;

import com.woongjin.survey.domain.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 회원 MyBatis Mapper 인터페이스
 */
@Mapper
public interface MemberMapper {

    // 로그인 ID로 단건 조회 (Spring Security 인증에서 사용)
    Optional<Member> findByLoginId(String loginId);

    // 회원 PK로 단건 조회 (토큰 재발급 시 사용)
    Optional<Member> findById(Long memberId);

    // 회원 등록 (초기 데이터 생성용)
    void insertMember(Member member);
}
