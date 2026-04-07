package com.woongjin.survey.global.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화
 * - 이 설정이 있어야 @CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate가 동작
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
