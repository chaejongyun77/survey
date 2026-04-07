package com.woongjin.survey.domain.auth.infra;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Spring Security의 UserDetails를 확장한 로그인 사용자 정보 객체
 * - CustomUserDetailsService에서 생성
 * - 컨트롤러에서 @AuthenticationPrincipal UserPrincipal 로 바로 꺼내 사용
 *
 * 사용 예시:
 *   @GetMapping
 *   public String page(@AuthenticationPrincipal UserPrincipal UserPrincipal) {
 *       String loginId = UserPrincipal.getLoginId();
 *       String name    = UserPrincipal.getName();
 *   }
 */
@Getter
public class UserPrincipal extends User {

    private final Long memberId;
    private final String name;

    public UserPrincipal(Long memberId, String loginId, String password,
                       String name, Collection<? extends GrantedAuthority> authorities) {
        super(loginId, password, authorities);
        this.memberId = memberId;
        this.name = name;
    }

    public String getLoginId() {
        return getUsername(); // User 클래스의 getUsername() = loginId
    }
}
