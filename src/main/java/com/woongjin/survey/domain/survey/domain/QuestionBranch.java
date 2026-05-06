package com.woongjin.survey.domain.survey.domain;

import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 문항 분기 엔티티 - SVY_QST_QTY 테이블 매핑
 *
 * 부모 문항의 특정 항목(PRNTS_ITM_ID)이 선택되었을 때
 * 자식 문항(CHLRN_QST_ID)이 활성화되는 조건분기 관계를 표현한다.
 *
 * audit 컬럼명이 BaseEntity(RCNT_UPDT_ID/DT) 와 달리 LAST_UPDT_ID/DT 를 사용하므로
 * @AttributeOverride 로 재정의한다.
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "SVY_QST_QTY")
@EntityListeners(AuditingEntityListener.class)
@AttributeOverrides({
        @AttributeOverride(name = "lastModifiedBy",   column = @Column(name = "LAST_UPDT_ID")),
        @AttributeOverride(name = "lastModifiedDate", column = @Column(name = "LAST_UPDT_DT"))
})
public class QuestionBranch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QST_QTY_ID")
    private Long id;

    /** 부모 문항 ID */
    @Column(name = "PRNTS_QST_ID", nullable = false)
    private Long parentQuestionId;

    /** 부모 항목 ID — 이 항목 선택 시 자식 문항이 활성화 */
    @Column(name = "PRNTS_ITM_ID", nullable = false)
    private Long parentItemId;

    /** 자식 문항 ID — 활성화 대상 */
    @Column(name = "CHLRN_QST_ID", nullable = false)
    private Long childQuestionId;
}
