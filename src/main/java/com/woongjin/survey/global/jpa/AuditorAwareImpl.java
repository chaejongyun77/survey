package com.woongjin.survey.global.jpa;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA Auditing 에서 "현재 사용자가 누구인지" 알려주는 클래스.
 *
 * [인증 주체별 처리]
 * - UserPrincipal : 직원 — JwtAuthenticationFilter 가 SecurityContext 에 세팅
 * - Long          : 설문 참여자 — ClientTokenFilter 가 SecurityContext 에 세팅
 *
 * [시스템 작업 처리]
 * - 인증 정보가 없는 컨텍스트(배치, DataInitializer 등)에서는 SYSTEM_USER_ID(0L) 반환
 *   → FRST_CRTN_ID / RCNT_UPDT_ID 의 NOT NULL 제약을 안전하게 만족
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    /** 시스템 작업(배치, 초기화 등)을 식별하는 의사 사용자 ID */
    public static final Long SYSTEM_USER_ID = 0L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // 비인증 컨텍스트 = 시스템 작업으로 간주
            return Optional.of(SYSTEM_USER_ID);
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal.getEmpId());
        }

        if (authentication.getPrincipal() instanceof Long empId) {
            return Optional.of(empId);
        }

        // 알 수 없는 Principal 타입도 시스템 작업으로 처리 (안전 기본값)
        return Optional.of(SYSTEM_USER_ID);
    }
}
