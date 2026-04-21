package com.woongjin.survey.global.jpa;

import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.global.jwt.ClientTokenProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

/**
 * JPA Auditing에서 "현재 사용자가 누구인지" 알려주는 클래스
 *
 * [우선순위]
 * 1) 관리자 측: SecurityContext — JwtAuthenticationFilter 가 세팅한 UserPrincipal
 * 2) 사용자 측: request attribute — ClientTokenFilter 가 이미 주입한 clientEmpId
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {

        // 1순위: 관리자 측 — SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            if ("system".equals(authentication.getPrincipal())) {
                return Optional.of(0L);
            }

            if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
                return Optional.of(userPrincipal.getEmpId());
            }
        }

        // 2순위: 사용자 측 — ClientTokenFilter 가 request attribute 에 주입한 empId
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            Object empId = attrs.getAttribute(
                    ClientTokenProvider.ATTR_EMP_ID, RequestAttributes.SCOPE_REQUEST);
            if (empId instanceof Long id) return Optional.of(id);
        }

        return Optional.empty();
    }
}