package com.woongjin.survey.domain.survey.domain;

import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설문 엔티티 - SVY_BSS_TB 테이블 매핑
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "svy_bss_tb")
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SVY_ID")
    private Long id;

    @Column(name = "SITE", nullable = false, length = 10)
    private String site;

    @Column(name = "SVY_SJ", nullable = false, length = 50)
    private String title;

    @Column(name = "SVY_DC", columnDefinition = "TEXT")
    private String description;

    @Column(name = "IMG_URL", length = 255)
    private String imgUrl;

    /** PC / MOBILE / ALL */
    @Column(name = "DVCE_TYPE", nullable = false, length = 5)
    private String deviceType;

    @Column(name = "BGN_DT", nullable = false)
    private LocalDateTime beginDate;

    @Column(name = "END_DT", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "ENFRT_YN", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean enforced;

    /** INTERN / LEADER / SELECTION */
    @Column(name = "TRPSN_TY", nullable = false, length = 10)
    private String targetPersonType;

    /** MALE / FEMALE / ALL */
    @Column(name = "SXDN", nullable = false, length = 10)
    private String gender;

    @Column(name = "MIN_AGE", nullable = false)
    private Integer minAge;

    @Column(name = "MXMM_AGE", nullable = false)
    private Integer maxAge;

    @Column(name = "USE_YN", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean useYn;

    /** DRAFT / COMPLETED / APPROVED */
    @Column(name = "SVY_STS", nullable = false, length = 10)
    private String status;

    @Column(name = "DEL_DT")
    private LocalDateTime deletedDate;
}
