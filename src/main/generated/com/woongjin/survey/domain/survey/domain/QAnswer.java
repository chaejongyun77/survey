package com.woongjin.survey.domain.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAnswer is a Querydsl query type for Answer
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAnswer extends EntityPathBase<Answer> {

    private static final long serialVersionUID = 1847294358L;

    public static final QAnswer answer = new QAnswer("answer");

    public final com.woongjin.survey.global.jpa.QBaseEntity _super = new com.woongjin.survey.global.jpa.QBaseEntity(this);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public final SimplePath<java.util.List<com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto>> answers =
            createSimple("answers", (Class) java.util.List.class);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> empId = createNumber("empId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final NumberPath<Long> surveyId = createNumber("surveyId", Long.class);

    public QAnswer(String variable) {
        super(Answer.class, forVariable(variable));
    }

    public QAnswer(Path<? extends Answer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAnswer(PathMetadata metadata) {
        super(Answer.class, metadata);
    }

}
