package com.woongjin.survey.domain.statistics.service;

import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionMetaDto;
import com.woongjin.survey.domain.statistics.dto.RespondentAnswerDto;
import com.woongjin.survey.domain.statistics.dto.ResponseListResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * 설문 통계 — 조회 전용 서비스
 *
 * [책임]
 * - Repository 가 가져온 raw 데이터를 DTO 팩토리 메소드를 통해 응답 DTO 로 조립
 * - isDeadlineToday 등 여러 DTO 생성에 필요한 컨텍스트 값 계산
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsQueryService {

    private static final double LOW_RATE_THRESHOLD = 70.0;
    private static final int RESPONSE_PREVIEW_LIMIT = 50;

    private final StatisticsRepository statisticsRepository;
    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    public StatisticsSummaryResponse getSummary(Long surveyId) {
        SurveySummaryProjection p = statisticsRepository.findSummaryBySurveyId(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));
        return StatisticsSummaryResponse.from(p);
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
                .map(p -> {
                    double rate = calculateRate(p.respondedCount(), p.targetCount());
                    return DeptResponseRateResponse.from(p, rate, isDeadlineToday && rate < LOW_RATE_THRESHOLD);
                })
                .sorted(Comparator.comparingDouble(DeptResponseRateResponse::responseRate).reversed())
                .toList();
    }

    private static double calculateRate(long respondedCnt, long targetCnt) {
        if (targetCnt == 0) return 0.0;
        return Math.round((double) respondedCnt / targetCnt * 100 * 10) / 10.0;
    }

    /**
     * 응답자별 문항답변 — 최근 N건 미리보기 조회
     *
     * - questions : 동적 컬럼 헤더 + 라벨 매핑 데이터 (전체)
     * - responses : limit 으로 자른 최근 응답 (현재 50건)
     * - totalCount: 전체 응답 수 ("총 X건 중 N건 표시" 안내용)
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
                statisticsRepository.findRecentResponses(surveyId, RESPONSE_PREVIEW_LIMIT);

        int totalCount = statisticsRepository.countResponses(surveyId);

        return new ResponseListResponse(questions, responses, totalCount, RESPONSE_PREVIEW_LIMIT);
    }
}
