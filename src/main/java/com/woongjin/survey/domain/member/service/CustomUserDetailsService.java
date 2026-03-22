package com.woongjin.survey.domain.member.service;

import com.woongjin.survey.domain.member.entity.Member;
import com.woongjin.survey.domain.member.repository.MemberMapper;
import com.woongjin.survey.global.auth.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다: " + loginId));

        if (!"ACTIVE".equals(member.getStatus())) {
            throw new UsernameNotFoundException("비활성화된 계정입니다: " + loginId);
        }

        return new LoginMember(
                member.getMemberId(),
                member.getLoginId(),
                member.getPassword(),
                member.getName(),
                List.of(new SimpleGrantedAuthority(member.getRole()))
        );
    }
}
