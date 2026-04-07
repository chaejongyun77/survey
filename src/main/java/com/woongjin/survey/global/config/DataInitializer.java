package com.woongjin.survey.global.config;

import com.woongjin.survey.domain.member.domain.Member;
import com.woongjin.survey.domain.member.repository.MemberRepository;
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

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${INIT_ADMIN_ID}")
    private String adminId;
    @Value("${INIT_ADMIN_PW}")
    private String adminPw;
    @Value("${INIT_ADMIN_NAME}")
    private String adminName;
    @Value("${INIT_ADMIN_EMAIL}")
    private String adminEmail;


    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent(adminId, adminPw, adminName, adminEmail, "ROLE_ADMIN");
        log.info("=== 초기 계정 초기화 완료 ===");
    }

    private void createIfAbsent(String loginId, String rawPassword,
                                String name, String email, String role) {
        if (memberRepository.findByLoginIdAndStatus(loginId, "ACTIVE").isEmpty()) {
            Member member = Member.builder()
                    .loginId(loginId)
                    .password(passwordEncoder.encode(rawPassword))
                    .name(name)
                    .email(email)
                    .role(role)
                    .status("ACTIVE")
                    .build();
            memberRepository.save(member);
            log.info("계정 생성: {}", loginId);
        }
    }
}
