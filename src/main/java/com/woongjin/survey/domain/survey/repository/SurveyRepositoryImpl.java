package com.woongjin.survey.domain.survey.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woongjin.survey.domain.survey.domain.QSurvey;
import com.woongjin.survey.domain.survey.domain.QSurveyTargetPerson;
import com.woongjin.survey.domain.survey.dto.SurveyIntroDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SurveyIntroDto> findIntroById(Long surveyId) {

        QSurvey s = QSurvey.survey;
        QSurveyTargetPerson tp = QSurveyTargetPerson.surveyTargetPerson;

        LocalDateTime now = LocalDateTime.now();

        SurveyIntroDto result = queryFactory
                .select(Projections.constructor(SurveyIntroDto.class,
                        s.id,
                        s.title,
                        s.beginDate,
                        s.endDate,
                        // BGN_DT <= now <= END_DT 이면 진행중
                        s.beginDate.loe(now).and(s.endDate.goe(now)),
                        // 대상자 수 서브쿼리
                        com.querydsl.jpa.JPAExpressions
                                .select(tp.count())
                                .from(tp)
                                .where(tp.survey.id.eq(surveyId))
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
}
