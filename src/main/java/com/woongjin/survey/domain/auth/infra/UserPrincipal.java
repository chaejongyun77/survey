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
 *   public String page(@AuthenticationPrincipal UserPrincipal principal) {
 *       String empNo   = principal.getEmpNo();
 *       String empName = principal.getEmpName();
 *   }
 */
@Getter
public class UserPrincipal extends User {

    private final Long empId;
    private final String empName;

    public UserPrincipal(Long empId, String empNo, String password,
                         String empName, Boolean empStatus, Collection<? extends GrantedAuthority> authorities) {
        super(empNo, password, empStatus, true, true, true, authorities);
        this.empId   = empId;
        this.empName = empName;
    }


}
