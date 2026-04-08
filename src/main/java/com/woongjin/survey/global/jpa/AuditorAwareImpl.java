package com.woongjin.survey.global.jpa;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA Auditing에서 "현재 사용자가 누구인지" 알려주는 클래스
 * - @CreatedBy → 엔티티 생성 시 이 클래스에서 반환한 값이 들어감
 * - @LastModifiedBy → 엔티티 수정 시 이 클래스에서 반환한 값이 들어감
 *
 * 동작 원리:
 * JPA가 엔티티를 저장/수정할 때 "생성자/수정자를 뭘로 채울지?"
 * → AuditorAware의 getCurrentAuditor() 호출
 * → SecurityContext에서 로그인한 사용자의 loginId를 꺼내서 반환
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

        // DataInitializer 등 시스템 작업 시 "system" principal 사용 → 0L 반환
        if ("system".equals(authentication.getPrincipal())) {
            return Optional.of(0L);
        }

        // UserPrincipal에서 empId 추출
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal.getEmpId());
        }

        return Optional.empty();
    }
}