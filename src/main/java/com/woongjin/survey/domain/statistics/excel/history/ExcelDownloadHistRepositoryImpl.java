package com.woongjin.survey.domain.statistics.excel.history;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woongjin.survey.domain.employee.domain.QDepartment;
import com.woongjin.survey.domain.employee.domain.QEmployee;
import com.woongjin.survey.domain.survey.domain.QSurvey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 엑셀 다운로드 이력 — QueryDSL 동적 조회 구현.
 *
 * [공통 join 규칙]
 *  - createdBy(=EMP_ID) 로 Employee 와 명시 join
 *    (ExcelDownloadHist 에 @JoinColumn 을 또 걸면 @CreatedBy 와 충돌하므로 명시 join 으로만 처리)
 *  - Employee → Department 는 FK join
 */
@Repository
@RequiredArgsConstructor
public class ExcelDownloadHistRepositoryImpl implements ExcelDownloadHistRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QExcelDownloadHist hist = QExcelDownloadHist.excelDownloadHist;
    private static final QEmployee          emp  = QEmployee.employee;
    private static final QDepartment        dept = QDepartment.department;
    private static final QSurvey            svy  = QSurvey.survey;

    @Override
    public Page<ExcelDownloadHistListProjection> findAllPage(Pageable pageable) {

        List<ExcelDownloadHistListProjection> content = queryFactory
                .select(Projections.constructor(ExcelDownloadHistListProjection.class,
                        hist.surveyId,
                        svy.title,
                        emp.empNo,
                        emp.empName,
                        dept.deptName,
                        hist.createdDate
                ))
                .from(hist)
                .join(svy).on(svy.id.eq(hist.surveyId))
                .join(emp).on(emp.id.eq(hist.createdBy))
                .join(emp.department, dept)
                .orderBy(hist.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hist.count())
                .from(hist);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
