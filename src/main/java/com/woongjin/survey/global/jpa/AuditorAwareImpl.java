package com.woongjin.survey.global.jpa;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA Auditing에서 "현재 사용자가 누구인지" 알려주는 클래스
 *
 * [인증 주체별 처리]
 * - UserPrincipal : 직원 — JwtAuthenticationFilter 가 SecurityContext 에 세팅
 * - Long          : 설문 참여자 — ClientTokenFilter 가 SecurityContext 에 세팅
 * - "system"      : 배치/내부 처리 전용 (empId = 0)
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        if ("system".equals(authentication.getPrincipal())) {
            return Optional.of(0L);
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal.getEmpId());
        }

        if (authentication.getPrincipal() instanceof Long empId) {
            return Optional.of(empId);
        }

        return Optional.empty();
    }
}