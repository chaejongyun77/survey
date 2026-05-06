package com.woongjin.survey.domain.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuestionBranch is a Querydsl query type for QuestionBranch
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionBranch extends EntityPathBase<QuestionBranch> {

    private static final long serialVersionUID = 740206934L;

    public static final QQuestionBranch questionBranch = new QQuestionBranch("questionBranch");

    public final com.woongjin.survey.global.jpa.QBaseEntity _super = new com.woongjin.survey.global.jpa.QBaseEntity(this);

    public final NumberPath<Long> childQuestionId = createNumber("childQuestionId", Long.class);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final NumberPath<Long> parentItemId = createNumber("parentItemId", Long.class);

    public final NumberPath<Long> parentQuestionId = createNumber("parentQuestionId", Long.class);

    public QQuestionBranch(String variable) {
        super(QuestionBranch.class, forVariable(variable));
    }

    public QQuestionBranch(Path<? extends QuestionBranch> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuestionBranch(PathMetadata metadata) {
        super(QuestionBranch.class, metadata);
    }

}

