package com.woongjin.survey.domain.statistics.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woongjin.survey.domain.employee.domain.QDepartment;
import com.woongjin.survey.domain.employee.domain.QEmployee;
import com.woongjin.survey.domain.statistics.dto.QuestionItemMetaDto;
import com.woongjin.survey.domain.statistics.dto.QuestionMetaDto;
import com.woongjin.survey.domain.statistics.dto.RespondentAnswerDto;
import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;
import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;
import com.woongjin.survey.domain.survey.domain.QAnswer;
import com.woongjin.survey.domain.survey.domain.QQuestion;
import com.woongjin.survey.domain.survey.domain.QQuestionItem;
import com.woongjin.survey.domain.survey.domain.QSurvey;
import com.woongjin.survey.domain.survey.domain.QSurveyTargetPerson;
import com.woongjin.survey.domain.survey.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 설문 통계 Repository 구현체
 *
 * [쿼리 설계 — findSummaryBySurveyId]
 * - 설문 메타(SVY_BSS_TB) 1행을 조회하면서, 카운트 3종을 서브쿼리로 함께 추출
 * - 한 번의 라운드트립으로 끝내기 위함 (네트워크/세션 비용 절감)
 *
 *   문항 수    : SVY_QST_TB 에서 deletedAt is null
 *   대상자 수  : SVY_TRPSN_TB 카운트
 *   응답 수    : SVY_RSPN_TB 카운트 (한 사원이 한 설문에 1행)
 *
 * [쿼리 설계 — findDeptResponseRates]
 * - 대상자 테이블(SVY_TRPSN_TB)을 기준으로 EMP→DEPT 따라가서 부서별 GROUP BY
 * - 응답 테이블은 LEFT JOIN — 응답하지 않은 대상자는 카운트 0으로 포함
 * - count(r.empId)는 NULL 을 세지 않으므로 자연스럽게 응답자 수가 됨
 *
 * [쿼리 설계 — findRecentResponses]
 * - Answer + Employee + Department JOIN 한 번에 (N+1 방지)
 * - QST_ANSWR JSON 은 Hibernate 가 List<SurveyAnswerDto> 로 자동 매핑
 * - 정렬: createdDate DESC (최근 응답 우선)
 *
 * [쿼리 설계 — findQuestionsWithItems]
 * - fetchJoin 으로 Question + QuestionItem 한 번에 가져오고, 메모리에서 DTO 변환
 * - GroupBy.transform() 미사용 — Hibernate 6.6+ 에서 ScrollableResults 호환 문제 있음
 */
@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SurveySummaryProjection> findSummaryBySurveyId(Long surveyId) {

        QSurvey s              = QSurvey.survey;
        QQuestion q            = QQuestion.question;
        QSurveyTargetPerson tp = QSurveyTargetPerson.surveyTargetPerson;
        QAnswer a              = QAnswer.answer;

        SurveySummaryProjection result = queryFactory
                .select(Projections.constructor(SurveySummaryProjection.class,
                        s.id,
                        s.title,
                        s.site,
                        s.beginDate,
                        s.endDate,
                        // 문항 수 (미삭제)
                        JPAExpressions
                                .select(q.count())
                                .from(q)
                                .where(q.surveyId.eq(surveyId), q.deletedAt.isNull()),
                        // 대상자 수
                        JPAExpressions
                                .select(tp.count())
                                .from(tp)
                                .where(tp.survey.id.eq(surveyId)),
                        // 응답 수
                        JPAExpressions
                                .select(a.count())
                                .from(a)
                                .where(a.surveyId.eq(surveyId))
                ))
                .from(s)
                .where(
                        s.id.eq(surveyId),
                        s.deletedDate.isNull()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<DeptResponseRateProjection> findDeptResponseRates(Long surveyId) {

        QSurveyTargetPerson tp = QSurveyTargetPerson.surveyTargetPerson;
        QEmployee e            = QEmployee.employee;
        QDepartment d          = QDepartment.department;
        QAnswer a              = QAnswer.answer;

        return queryFactory
                .select(Projections.constructor(DeptResponseRateProjection.class,
                        d.id,
                        d.deptName,
                        tp.employee.id.count(),                  // 부서 내 대상자 수
                        a.empId.count()                          // LEFT JOIN 매칭된 응답자 수 (null 제외)
                ))
                .from(tp)
                .join(tp.employee, e)
                .join(e.department, d)
                .leftJoin(a).on(
                        a.surveyId.eq(tp.survey.id),
                        a.empId.eq(tp.employee.id)
                )
                .where(tp.survey.id.eq(surveyId))
                .groupBy(d.id, d.deptName)
                .fetch();
    }

    @Override
    public List<RespondentAnswerDto> findRecentResponses(Long surveyId, int limit) {

        QAnswer a     = QAnswer.answer;
        QEmployee e   = QEmployee.employee;
        QDepartment d = QDepartment.department;

        return queryFactory
                .select(Projections.constructor(RespondentAnswerDto.class,
                        e.empName,
                        d.deptName,
                        a.createdDate,   // BaseEntity 의 FRST_CRTN_DT
                        a.answers
                ))
                .from(a)
                .join(e).on(e.id.eq(a.empId))
                .join(e.department, d)
                .where(a.surveyId.eq(surveyId))
                .orderBy(a.createdDate.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<QuestionMetaDto> findQuestionsWithItems(Long surveyId) {

        QQuestion q     = QQuestion.question;
        QQuestionItem i = QQuestionItem.questionItem;

        // GroupBy.transform() 대신 fetchJoin + 메모리 변환 사용
        // → GroupBy.transform()은 Hibernate 6.6+ 와 ScrollableResults 호환 문제 있음
        List<Question> result = queryFactory
                .selectFrom(q)
                .leftJoin(q.items, i).fetchJoin()
                .where(
                        q.surveyId.eq(surveyId),
                        q.deletedAt.isNull()
                )
                .orderBy(q.sortOrder.asc(), i.sortOrder.asc())
                .distinct()
                .fetch();

        return result.stream()
                .map(this::toQuestionMetaDto)
                .toList();
    }

    private QuestionMetaDto toQuestionMetaDto(Question q) {
        List<QuestionItemMetaDto> items = q.getItems().stream()
                .map(item -> new QuestionItemMetaDto(
                        item.getId(),
                        item.getItemName(),
                        item.getSortOrder()
                ))
                .toList();

        return new QuestionMetaDto(
                q.getId(),
                q.getQuestionName(),
                q.getQuestionType(),
                q.getSortOrder(),
                items
        );
    }
}
