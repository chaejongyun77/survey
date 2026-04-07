package com.woongjin.survey.domain.auth.infra;

import com.woongjin.survey.domain.member.domain.Member;
import com.woongjin.survey.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security UserDetailsService 구현체 (Adapter — infra)
 * - Spring Security라는 외부 프레임워크와의 연결 지점
 * - DB에서 사용자 조회 → UserPrincipal 반환
 * - /infra 에 위치하는 이유: Spring Security에 강하게 결합된 외부 기술 구현체
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByLoginIdAndStatus(loginId, "ACTIVE")
                .orElseThrow(() -> new UsernameNotFoundException(
                        "존재하지 않거나 비활성화된 계정입니다: " + loginId));

        return new UserPrincipal(
                member.getMemberId(),
                member.getLoginId(),
                member.getPassword(),
                member.getName(),
                List.of(new SimpleGrantedAuthority(member.getRole()))
        );
    }
}
