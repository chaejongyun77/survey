package com.woongjin.survey.domain.statistics.domain;

import com.woongjin.survey.domain.statistics.domain.statresult.QuestionStatResult;
import com.woongjin.survey.domain.survey.domain.Question;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 설문 문항별 통계 — SVY_QST_STAT_TB 매핑.
 *
 * [성격]
 * - 배치가 30분 주기로 통째로 갈아끼우는 캐시성 테이블
 * - Survey/Question 과 FK 매핑은 의도적으로 두지 않음 (결합도 ↓)
 *   필요한 정보(타입 등)는 자체 컬럼으로 보유
 *
 * [데이터 구조]
 * - 한 행 = (설문 1, 문항 1)의 집계 결과
 * - statData 는 문항 타입별로 다른 record 가 들어감 (sealed interface)
 * - DB 에는 JSON 으로 저장되며 Jackson 이 자동 직/역직렬화
 */
@Getter
@Entity
@Table(
        name = "svy_qst_stat_tb",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_svy_qst",
                columnNames = {"SVY_ID", "QST_ID"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionStat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STAT_ID")
    private Long id;

    @Column(name = "SVY_ID", nullable = false)
    private Long surveyId;

    @Column(name = "QST_ID", nullable = false)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "QST_TY_CD", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(name = "TOTAL_RSPN_CNT", nullable = false)
    private int totalResponseCount;

    /**
     * 문항 타입별 집계 결과.
     * @JdbcTypeCode(JSON) — Hibernate 가 JSON 컬럼으로 매핑
     * 실제 직렬화/역직렬화는 Jackson 이 담당
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "STAT_DATA", nullable = false, columnDefinition = "JSON")
    private QuestionStatResult statData;

    @Column(name = "AGGRGT_DT", nullable = false)
    private LocalDateTime aggregatedAt;

    public static QuestionStat from(Question question, int responseCount,
                                    QuestionStatResult statData, LocalDateTime aggregatedAt) {
        return builder()
                .surveyId(question.getSurveyId())
                .questionId(question.getId())
                .questionType(question.getQuestionType())
                .totalResponseCount(responseCount)
                .statData(statData)
                .aggregatedAt(aggregatedAt)
                .build();
    }

    @Builder
    private QuestionStat(Long surveyId,
                         Long questionId,
                         QuestionType questionType,
                         int totalResponseCount,
                         QuestionStatResult statData,
                         LocalDateTime aggregatedAt) {
        this.surveyId           = surveyId;
        this.questionId         = questionId;
        this.questionType       = questionType;
        this.totalResponseCount = totalResponseCount;
        this.statData           = statData;
        this.aggregatedAt       = aggregatedAt;
    }
}
