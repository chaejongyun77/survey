package com.woongjin.survey.domain.statistics.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woongjin.survey.domain.employee.domain.QDepartment;
import com.woongjin.survey.domain.employee.domain.QEmployee;
import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;
import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;
import com.woongjin.survey.domain.survey.domain.QAnswer;
import com.woongjin.survey.domain.survey.domain.QQuestion;
import com.woongjin.survey.domain.survey.domain.QSurvey;
import com.woongjin.survey.domain.survey.domain.QSurveyTargetPerson;
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
 */
@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SurveySummaryProjection> findSummaryBySurveyId(Long surveyId) {

        QSurvey s             = QSurvey.survey;
        QQuestion q           = QQuestion.question;
        QSurveyTargetPerson tp = QSurveyTargetPerson.surveyTargetPerson;
        QAnswer a             = QAnswer.answer;

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
}

