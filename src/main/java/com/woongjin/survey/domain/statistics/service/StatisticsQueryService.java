package com.woongjin.survey.domain.statistics.service;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionMetaDto;
import com.woongjin.survey.domain.statistics.dto.RespondentAnswerDto;
import com.woongjin.survey.domain.statistics.dto.ResponseListResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;
import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;
import com.woongjin.survey.domain.statistics.repository.StatisticsRepository;
import com.woongjin.survey.domain.survey.domain.Survey;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 설문 통계 — 조회 전용 서비스
 *
 * [책임]
 * - Repository 가 가져온 raw 카운트를 화면용 응답 DTO 로 조립
 * - 응답률, 미응답 수, 마감까지 남은 일수, 회색 표시 룰 등 가공 값 계산
 *
 * [비즈니스 규칙]
 * - 응답률: 응답수 / 대상자수 × 100, 소수점 첫째자리까지 (대상자 0이면 0.0)
 * - 남은 일수: 오늘 ~ 종료일 (이미 지났으면 0)
 * - 회색 표시(lowRate): 마감일 당일이면서 응답률 70% 미만인 부서
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsQueryService {

    private static final double LOW_RATE_THRESHOLD = 70.0;
    private static final int RESPONDENT_PREVIEW_LIMIT = 30;

    private final StatisticsRepository statisticsRepository;
    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    public StatisticsSummaryResponse getSummary(Long surveyId) {

        SurveySummaryProjection p = statisticsRepository.findSummaryBySurveyId(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        int targetCnt    = (int) p.totalTargetCount();
        int respondedCnt = (int) p.respondedCount();
        int notResponded = Math.max(targetCnt - respondedCnt, 0);
        double rate      = calculateRate(respondedCnt, targetCnt);
        long daysLeft    = calculateDaysLeft(p.endDate());

        return StatisticsSummaryResponse.from(p, notResponded, rate, daysLeft);
    }

    /**
     * 조직(부서)별 응답률 조회
     * - 응답률 내림차순 정렬
     * - 마감일 당일이고 70% 미만인 경우 lowRate=true (회색 표시)
     */
    @Transactional(readOnly = true)
    public List<DeptResponseRateResponse> getDeptResponseRates(Long surveyId) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));

        boolean isDeadlineToday = LocalDate.now().isEqual(survey.getEndDate().toLocalDate());

        return statisticsRepository.findDeptResponseRates(surveyId).stream()
                .map(p -> toResponse(p, isDeadlineToday))
                .sorted(Comparator.comparingDouble(DeptResponseRateResponse::responseRate).reversed())
                .toList();
    }

    private DeptResponseRateResponse toResponse(DeptResponseRateProjection p, boolean isDeadlineToday) {
        int targetCnt    = (int) p.targetCount();
        int respondedCnt = (int) p.respondedCount();
        double rate      = calculateRate(respondedCnt, targetCnt);
        boolean lowRate  = isDeadlineToday && rate < LOW_RATE_THRESHOLD;

        return DeptResponseRateResponse.from(p, rate, lowRate);
    }

    /** 응답률 계산 — 소수점 첫째자리, 분모 0 방어 */
    private double calculateRate(int respondedCnt, int targetCnt) {
        if (targetCnt == 0) return 0.0;
        double raw = (double) respondedCnt / targetCnt * 100;
        return Math.round(raw * 10) / 10.0;
    }

    /** 남은 일수 — 종료일 지났으면 0 */
    private long calculateDaysLeft(LocalDateTime endDate) {
        long days = Duration.between(LocalDateTime.now(), endDate).toDays();
        return Math.max(days, 0);
    }

    /**
     * 응답자별 문항답변 — 최근 N건 미리보기 조회
     *
     * - questions : 동적 컬럼 헤더 + 라벨 매핑 데이터 (전체, limit 없음)
     * - responses : 최근 응답자 (현재 30건)
     *
     * 전체 응답은 엑셀 다운로드 API 로 별도 제공
     */
    @Transactional(readOnly = true)
    public ResponseListResponse getResponseList(Long surveyId) {

        // 설문 존재 여부 확인 (없으면 404)
        if (!surveyRepository.existsById(surveyId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }

        List<QuestionMetaDto> questions = statisticsRepository.findQuestionsWithItems(surveyId);

        List<RespondentAnswerDto> responses =
                statisticsRepository.findRecentResponses(surveyId, RESPONDENT_PREVIEW_LIMIT);

        return new ResponseListResponse(questions, responses);
    }
}
