package com.woongjin.survey.domain.member.service;

import com.woongjin.survey.domain.member.entity.Member;
import com.woongjin.survey.domain.member.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 인증에 사용되는 UserDetailsService 구현체
 * - 로그인 시 DB에서 사용자 정보를 조회하여 UserDetails 반환
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다: " + loginId));

        // 비활성 계정 체크
        if (!"ACTIVE".equals(member.getStatus())) {
            throw new UsernameNotFoundException("비활성화된 계정입니다: " + loginId);
        }

        return new User(
                member.getLoginId(),
                member.getPassword(),
                List.of(new SimpleGrantedAuthority(member.getRole()))
        );
    }
}
