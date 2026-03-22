package com.woongjin.survey.global.config;

import com.woongjin.survey.domain.member.entity.Member;
import com.woongjin.survey.domain.member.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 앱 시작 시 테스트 계정 자동 생성
 * - BCrypt 해시를 코드에서 직접 생성하므로 비밀번호가 항상 정확함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent("admin",  "1234", "관리자", "admin@woongjin.com",  "ROLE_ADMIN");
        createIfAbsent("user01", "1234", "홍길동", "user01@woongjin.com", "ROLE_USER");
        log.info("=== 테스트 계정 초기화 완료 ===");
        log.info("  admin  / 1234 (ROLE_ADMIN)");
        log.info("  user01 / 1234 (ROLE_USER)");
    }

    private void createIfAbsent(String loginId, String rawPassword,
                                String name, String email, String role) {
        if (memberMapper.findByLoginId(loginId).isEmpty()) {
            Member member = Member.builder()
                    .loginId(loginId)
                    .password(passwordEncoder.encode(rawPassword))
                    .name(name)
                    .email(email)
                    .role(role)
                    .status("ACTIVE")
                    .build();
            memberMapper.insertMember(member);
            log.info("계정 생성: {}", loginId);
        }
    }
}
