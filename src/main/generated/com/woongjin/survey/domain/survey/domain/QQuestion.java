package com.woongjin.survey.domain.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestion is a Querydsl query type for Question
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestion extends EntityPathBase<Question> {

    private static final long serialVersionUID = 482910375L;

    public static final QQuestion question = new QQuestion("question");

    public final com.woongjin.survey.global.jpa.QBaseEntity _super = new com.woongjin.survey.global.jpa.QBaseEntity(this);

    public final NumberPath<Long> childQuestionId = createNumber("childQuestionId", Long.class);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<QuestionItem, QQuestionItem> items = createList("items", QuestionItem.class, QQuestionItem.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final NumberPath<Long> parentItemId = createNumber("parentItemId", Long.class);

    public final StringPath questionName = createString("questionName");

    public final EnumPath<com.woongjin.survey.domain.survey.domain.enums.QuestionType> questionType =
            createEnum("questionType", com.woongjin.survey.domain.survey.domain.enums.QuestionType.class);

    public final BooleanPath required = createBoolean("required");

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public final NumberPath<Long> surveyId = createNumber("surveyId", Long.class);

    public QQuestion(String variable) {
        super(Question.class, forVariable(variable));
    }

    public QQuestion(Path<? extends Question> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuestion(PathMetadata metadata) {
        super(Question.class, metadata);
    }

}
