package com.woongjin.survey.domain.survey.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QQuestionItem is a Querydsl query type for QuestionItem
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuestionItem extends EntityPathBase<QuestionItem> {

    private static final long serialVersionUID = 1293847562L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QQuestionItem questionItem = new QQuestionItem("questionItem");

    public final com.woongjin.survey.global.jpa.QBaseEntity _super = new com.woongjin.survey.global.jpa.QBaseEntity(this);

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath itemName = createString("itemName");

    //inherited
    public final NumberPath<Long> lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final QQuestion question;

    public final NumberPath<Integer> sortOrder = createNumber("sortOrder", Integer.class);

    public QQuestionItem(String variable) {
        this(QuestionItem.class, forVariable(variable), INITS);
    }

    public QQuestionItem(Path<? extends QuestionItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QQuestionItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QQuestionItem(PathMetadata metadata, PathInits inits) {
        this(QuestionItem.class, metadata, inits);
    }

    public QQuestionItem(Class<? extends QuestionItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.question = inits.isInitialized("question") ? new QQuestion(forProperty("question")) : null;
    }

}
