package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.Survey;
import com.woongjin.survey.domain.survey.domain.enums.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long>, SurveyRepositoryCustom {

    /**
     * 통계 집계 대상 설문 ID 목록.
     *
     * [조건]
     *  - 사용중 (use_yn = true)
     *  - 삭제되지 않음 (del_dt IS NULL)
     *  - 승인 상태 (status = APPROVED)
     *  - 현재 기간 내 (begin_date <= now <= end_date)
     *
     * [용도] 통계 배치 Reader 가 청크 단위로 흘려보낼 ID 목록
     */
    @Query("""
            select s.id
            from Survey s
            where s.useYn = true
              and s.deletedDate is null
              and s.status = :status
              and s.beginDate <= :now
              and s.endDate   >= :now
            order by s.id
            """)
    List<Long> findActiveSurveyIds(SurveyStatus status, LocalDateTime now);
}
