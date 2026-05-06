package com.woongjin.survey.domain.statistics.repository;

import com.woongjin.survey.domain.statistics.dto.RespondentAnswerDto;
import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;
import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;
import com.woongjin.survey.domain.statistics.dto.QuestionMetaDto;

import java.util.List;
import java.util.Optional;

/**
 * 설문 통계 전용 Repository
 *
 * [역할]
 * - 통계 페이지에서 필요한 집계 쿼리만 담당
 * - 도메인 Repository(SurveyRepository)와 분리하여 책임 명확화
 */
public interface StatisticsRepository {

    /**
     * 설문 기본정보 요약 조회
     * - 설문 메타 + 문항/대상자/응답 카운트를 한 번에 조회
     */
    Optional<SurveySummaryProjection> findSummaryBySurveyId(Long surveyId);

    /**
     * 조직(부서)별 응답률 집계
     * - 대상자가 1명 이상인 부서만 반환
     * - 응답률 내림차순 정렬은 Service 레이어에서 수행
     */
    List<DeptResponseRateProjection> findDeptResponseRates(Long surveyId);

    /**
     * 응답자별 문항답변 — 최근 응답순 N건 조회 (미리보기용)
     *
     * - Answer + Employee + Department JOIN 한 번으로 처리 (N+1 방지)
     * - QST_ANSWR JSON 은 Hibernate 가 List<SurveyAnswerDto> 로 자동 매핑
     * - 정렬: FRST_CRTN_DT DESC (가장 최근 응답이 위로)
     *
     * @param surveyId 설문 ID
     * @param limit    최대 조회 건수 (예: 50)
     */
    List<RespondentAnswerDto> findRecentResponses(Long surveyId, int limit);

    /**
     * 응답자별 문항답변 — 전체 응답 수 카운트
     *
     * - findRecentResponses 가 limit 으로 자른 미리보기만 반환하므로,
     *   "전체 응답 X건 중 N건 표시" 안내 문구를 위해 별도 카운트 제공
     * - SVY_ID 인덱스 전제 (없으면 풀스캔)
     */
    int countResponses(Long surveyId);

    /**
     * 응답자별 문항답변 — 문항 메타(+ 항목) 조회
     *
     * - Question + QuestionItem fetch join (한 번의 쿼리)
     * - 미삭제 문항만 / 항목은 전체 (삭제된 항목도 포함 → 과거 응답 매핑용)
     * - 정렬: 문항 sortOrder ASC
     */
    List<QuestionMetaDto> findQuestionsWithItems(Long surveyId);

}
