package com.woongjin.survey.domain.auth.infra;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeRole;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
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

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String empNo) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmpNo(empNo)
                .orElseThrow(() -> new UsernameNotFoundException("등록되지 않은 아이디입니다."));

        // 2. 퇴사자 체크 (401 계열)
        if (!employee.getEmpStatus()) {
            throw new DisabledException("EMP_STATUS_FALSE");
        }

        // 3. 권한 체크 (401 계열)
        if (employee.getRole() == EmployeeRole.USER) {
            // 일반 유저는 관리자 자격이 없으므로 '자격 미달' 예외 발생
            throw new InsufficientAuthenticationException("ROLE_USER_NOT_ALLOWED") ;
        }

        return new UserPrincipal(
                employee.getId(),
                employee.getEmpNo(),
                employee.getEmpPw(),
                employee.getEmpName(),
                List.of(new SimpleGrantedAuthority(employee.getRole().name()))
        );
    }
}
