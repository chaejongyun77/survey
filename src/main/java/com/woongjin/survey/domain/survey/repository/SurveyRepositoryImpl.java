package com.woongjin.survey.domain.survey.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woongjin.survey.domain.survey.domain.QSurvey;
import com.woongjin.survey.domain.survey.domain.QSurveyQuestion;
import com.woongjin.survey.domain.survey.domain.QSurveyTargetPerson;
import com.woongjin.survey.domain.survey.domain.enums.SurveyStatus;
import com.woongjin.survey.domain.survey.dto.SurveyIntroResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SurveyIntroResponse> findIntroById(Long surveyId) {

        QSurvey s               = QSurvey.survey;
        QSurveyTargetPerson tp  = QSurveyTargetPerson.surveyTargetPerson;
        QSurveyQuestion q       = QSurveyQuestion.surveyQuestion;

        SurveyIntroResponse result = queryFactory
                .select(Projections.constructor(SurveyIntroResponse.class,
                        s.id,
                        s.title,
                        s.beginDate,
                        s.endDate,
                        // 대상자 수 서브쿼리
                        com.querydsl.jpa.JPAExpressions
                                .select(tp.count())
                                .from(tp)
                                .where(tp.survey.id.eq(surveyId)),
                        // 문항 수 서브쿼리 (미삭제 기준)
                        com.querydsl.jpa.JPAExpressions
                                .select(q.count())
                                .from(q)
                                .where(q.surveyId.eq(surveyId), q.deletedAt.isNull())
                ))
                .from(s)
                .where(
                        s.id.eq(surveyId),
                        s.useYn.isTrue(),
                        s.deletedDate.isNull()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Long> findActiveSurveyIdByEmpId(Long empId) {

        QSurvey s              = QSurvey.survey;
        QSurveyTargetPerson tp = QSurveyTargetPerson.surveyTargetPerson;

        LocalDateTime now = LocalDateTime.now();

        Long surveyId = queryFactory
                .select(s.id)
                .from(s)
                .join(tp).on(tp.survey.id.eq(s.id).and(tp.employee.id.eq(empId)))
                .where(
                        s.useYn.isTrue(),
                        s.deletedDate.isNull(),
                        s.status.eq(SurveyStatus.APPROVED),
                        s.beginDate.loe(now),
                        s.endDate.goe(now)
                )
                .fetchFirst();

        return Optional.ofNullable(surveyId);
    }
}
