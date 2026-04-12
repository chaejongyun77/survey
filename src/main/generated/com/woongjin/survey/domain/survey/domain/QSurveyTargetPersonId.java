package com.woongjin.survey.domain.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSurveyTargetPersonId is a Querydsl query type for SurveyTargetPersonId
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSurveyTargetPersonId extends BeanPath<SurveyTargetPersonId> {

    private static final long serialVersionUID = -557814871L;

    public static final QSurveyTargetPersonId surveyTargetPersonId = new QSurveyTargetPersonId("surveyTargetPersonId");

    public final NumberPath<Long> empId = createNumber("empId", Long.class);

    public final NumberPath<Long> surveyId = createNumber("surveyId", Long.class);

    public QSurveyTargetPersonId(String variable) {
        super(SurveyTargetPersonId.class, forVariable(variable));
    }

    public QSurveyTargetPersonId(Path<? extends SurveyTargetPersonId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSurveyTargetPersonId(PathMetadata metadata) {
        super(SurveyTargetPersonId.class, metadata);
    }

}

