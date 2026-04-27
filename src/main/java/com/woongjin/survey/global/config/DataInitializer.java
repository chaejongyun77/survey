/*
package com.woongjin.survey.global.config;

import com.woongjin.survey.domain.employee.domain.Department;
import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeRole;
import com.woongjin.survey.domain.employee.domain.enums.Gender;
import com.woongjin.survey.domain.employee.domain.enums.Position;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import com.woongjin.survey.domain.survey.domain.Survey;
import com.woongjin.survey.domain.survey.domain.SurveyTargetPerson;
import com.woongjin.survey.domain.survey.domain.SurveyTargetPersonId;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import com.woongjin.survey.domain.survey.repository.SurveyTargetPersonRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

*/
/**
 * 앱 시작 시 초기 계정 자동 생성
 * - 계정 정보는 .env (local) 또는 OS 환경변수 (dev/prod)에서 주입
 * - 코드에 아이디/비밀번호가 노출되지 않음
 *//*

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final SurveyRepository surveyRepository;
    private final SurveyTargetPersonRepository surveyTargetPersonRepository;

    @Value("${INIT_ADMIN_ID}")
    private String adminEmpNo;
    @Value("${INIT_ADMIN_PW}")
    private String adminPw;
    @Value("${INIT_ADMIN_NAME}")
    private String adminName;
    @Value("${INIT_ADMIN_EMAIL}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // DataInitializer는 앱 시작 시 실행되므로 SecurityContext가 비어있음
        // → AuditorAware가 Optional.empty()를 반환 → createdBy가 null → DB 에러
        // 시스템 계정(id=0L)을 임시로 세팅해서 Auditing이 동작하게 함
        setSystemSecurityContext();
        try {
            createIfAbsent(adminEmpNo, adminPw, adminName, adminEmail);
            createSurveyDummyData();
            log.info("=== 초기 계정 초기화 완료 ===");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    */
/**
     * JPA Auditing용 시스템 사용자를 SecurityContext에 임시 세팅
     *//*

    private void setSystemSecurityContext() {
        UsernamePasswordAuthenticationToken systemAuth = new UsernamePasswordAuthenticationToken(
                "system", null, List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"))
        );
        SecurityContextHolder.getContext().setAuthentication(systemAuth);
    }

    private void createIfAbsent(String empNo, String rawPassword, String empName, String email) {
        if (employeeRepository.findByEmpNoAndEmpStatus(empNo, true).isEmpty()) {

            // dept_tb에 존재하는 첫 번째 부서를 admin 계정의 소속 부서로 사용
            Department department = entityManager
                    .createQuery("SELECT d FROM Department d ORDER BY d.id ASC", Department.class)
                    .setMaxResults(1)
                    .getSingleResult();

            Employee employee = Employee.builder()
                    .empNo(empNo)
                    .empPw(passwordEncoder.encode(rawPassword))
                    .empName(empName)
                    .email(email)
                    .role(EmployeeRole.ADMIN)
                    .empStatus(true)
                    .department(department)
                    .position(Position.SENIOR)
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .gender(Gender.MALE)
                    .build();

            employeeRepository.save(employee);
            log.info("초기 계정 생성: empNo={}", empNo);
        }
    }

    */
/**
     * 설문 + 대상자 더미데이터 생성
     * - surveyId=1, userId=1~4 에 대응
     * - 이미 존재하면 skip
     *//*

    private void createSurveyDummyData() {
        if (surveyRepository.count() > 0) {
            log.info("설문 더미데이터 이미 존재 — skip");
            return;
        }

        // 1) 설문 생성 (SVY_ID = 1)
        Survey survey = Survey.builder()
                .site("WJTB")
                .title("2026년 상반기 고객 만족도 조사")
                .description("웅진씽크빅 상반기 고객 만족도 설문입니다.")
                .imgUrl(null)
                .deviceType("ALL")
                .beginDate(LocalDateTime.of(2026, 3, 18, 0, 0))
                .endDate(LocalDateTime.of(2026, 12, 31, 23, 59))
                .essential(false)
                .targetPersonType("SELECTION")
                .gender("ALL")
                .minAge(20)
                .maxAge(60)
                .useYn(true)
                .status("APPROVED")
                .deletedDate(null)
                .build();

        surveyRepository.save(survey);
        log.info("설문 더미데이터 생성: surveyId={}", survey.getId());

        // 2) 실제 존재하는 직원 목록에서 최대 4명 조회해서 대상자로 등록
        List<Employee> employees = employeeRepository.findAll()
                .stream()
                .limit(4)
                .toList();

        for (Employee emp : employees) {
            SurveyTargetPerson targetPerson = SurveyTargetPerson.builder()
                    .id(new SurveyTargetPersonId(survey.getId(), emp.getId()))
                    .survey(survey)
                    .employee(emp)
                    .build();
            surveyTargetPersonRepository.save(targetPerson);
        }

        log.info("설문 대상자 더미데이터 생성: {}명", employees.size());
    }
}
*/
