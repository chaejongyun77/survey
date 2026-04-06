package com.woongjin.survey.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 필드
 * - 생성자, 생성일, 수정자, 수정일을 자동 관리
 *
 * 사용법: 엔티티 클래스에서 상속
 *   @Entity
 *   public class Survey extends BaseEntity { ... }
 *
 * @MappedSuperclass — JPA가 이 클래스의 필드를 상속받는 엔티티의 컬럼으로 인식
 * @EntityListeners  — 엔티티가 저장/수정될 때 Auditing 이벤트를 감지
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /** 생성자 — 엔티티 최초 저장 시 로그인한 사용자 ID 자동 세팅 */
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    /** 생성일 — 엔티티 최초 저장 시 현재 시간 자동 세팅 */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    /** 수정자 — 엔티티 수정 시 로그인한 사용자 ID 자동 세팅 */
    @LastModifiedBy
    private String lastModifiedBy;

    /** 수정일 — 엔티티 수정 시 현재 시간 자동 세팅 */
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
