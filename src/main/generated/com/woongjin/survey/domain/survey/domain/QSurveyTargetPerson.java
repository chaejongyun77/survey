package com.woongjin.survey.domain.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyTargetPerson is a Querydsl query type for SurveyTargetPerson
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyTargetPerson extends EntityPathBase<SurveyTargetPerson> {

    private static final long serialVersionUID = -1028512274L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurveyTargetPerson surveyTargetPerson = new QSurveyTargetPerson("surveyTargetPerson");

    public final com.woongjin.survey.global.jpa.QBaseEntity _super = new com.woongjin.survey.global.jpa.QBaseEntity(this);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final com.woongjin.survey.domain.employee.domain.QEmployee employee;

    public final QSurveyTargetPersonId id;

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final QSurvey survey;

    public QSurveyTargetPerson(String variable) {
        this(SurveyTargetPerson.class, forVariable(variable), INITS);
    }

    public QSurveyTargetPerson(Path<? extends SurveyTargetPerson> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurveyTargetPerson(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurveyTargetPerson(PathMetadata metadata, PathInits inits) {
        this(SurveyTargetPerson.class, metadata, inits);
    }

    public QSurveyTargetPerson(Class<? extends SurveyTargetPerson> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.employee = inits.isInitialized("employee") ? new com.woongjin.survey.domain.employee.domain.QEmployee(forProperty("employee"), inits.get("employee")) : null;
        this.id = inits.isInitialized("id") ? new QSurveyTargetPersonId(forProperty("id")) : null;
        this.survey = inits.isInitialized("survey") ? new QSurvey(forProperty("survey")) : null;
    }

}

