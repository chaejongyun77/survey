package com.woongjin.survey.global.config;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeRole;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeStatus;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 초기 계정 자동 생성
 * - 계정 정보는 .env (local) 또는 OS 환경변수 (dev/prod)에서 주입
 * - 코드에 아이디/비밀번호가 노출되지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${INIT_ADMIN_ID}")
    private String adminEmpNo;
    @Value("${INIT_ADMIN_PW}")
    private String adminPw;
    @Value("${INIT_ADMIN_NAME}")
    private String adminName;
    @Value("${INIT_ADMIN_EMAIL}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent(adminEmpNo, adminPw, adminName, adminEmail);
        log.info("=== 초기 계정 초기화 완료 ===");
    }

    private void createIfAbsent(String empNo, String rawPassword, String empName, String email) {
        if (employeeRepository.findByEmpNoAndStatus(empNo, EmployeeStatus.ACTIVE).isEmpty()) {
            Employee employee = Employee.builder()
                    .empNo(empNo)
                    .password(passwordEncoder.encode(rawPassword))
                    .empName(empName)
                    .email(email)
                    .role(EmployeeRole.ADMIN)
                    .status(EmployeeStatus.ACTIVE)
                    .build();
            employeeRepository.save(employee);
            log.info("초기 계정 생성: empNo={}", empNo);
        }
    }
}
