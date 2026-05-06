package com.woongjin.survey.domain.survey.domain;

import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 설문 문항 엔티티 - SVY_QST_TB 테이블 매핑
 *
 * audit 컬럼명이 BaseEntity(FRST_CRTN_ID/LAST_UPDT_ID)와 달리
 * RCNT_UPDT_ID/RCNT_UPDT_DT 를 사용하므로 @AttributeOverride 로 재정의
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "SVY_QST_TB")
@EntityListeners(AuditingEntityListener.class)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QST_ID")
    private Long id;

    /** 소속 설문 ID */
    @Column(name = "SVY_ID", nullable = false)
    private Long surveyId;

    /** 문항 유형 (SINGLE_CHOICE / MULTIPLE_CHOICE / SUBJECTIVE / SCALE / RANKING) */
    @Enumerated(EnumType.STRING)
    @Column(name = "QST_TYPE", nullable = false, length = 10)
    private QuestionType questionType;

    /** 문항 명 */
    @Column(name = "QST_NM", nullable = false, length = 50)
    private String questionName;

    /** 필수 응답 여부 */
    @Column(name = "ESNTL_RSPN_YN", nullable = false)
    private Boolean required;

    /** 정렬 순서 */
    @Column(name = "SORT_ODR", nullable = false)
    private Integer sortOrder;

    /** 삭제 일시 (NULL 이면 미삭제) */
    @Column(name = "DEL_AT")
    private LocalDateTime deletedAt;

    /** 문항 항목 목록 (옵션) — TEXT 유형은 비어있음 */
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<QuestionItem> items = new ArrayList<>();
}
