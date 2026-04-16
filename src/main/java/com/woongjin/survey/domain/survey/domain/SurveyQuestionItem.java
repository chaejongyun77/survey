package com.woongjin.survey.domain.survey.domain;

import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설문 문항 항목(옵션) 엔티티 - SVY_QST_ITM_TB 테이블 매핑
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "SVY_QST_ITM_TB")
public class SurveyQuestionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QST_ITM_ID")
    private Long id;

    /** 소속 문항 (연관관계 주인) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QST_ID", nullable = false)
    private SurveyQuestion question;

    /** 항목 명 */
    @Column(name = "ITM_NM", nullable = false, length = 50)
    private String itemName;

    /** 정렬 순서 */
    @Column(name = "SORT_ODR", nullable = false)
    private Integer sortOrder;

    /** 삭제 일시 (NULL 이면 미삭제) */
    @Column(name = "DEL_DT")
    private LocalDateTime deletedAt;
}
