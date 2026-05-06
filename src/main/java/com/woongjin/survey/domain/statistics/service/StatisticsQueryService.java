package com.woongjin.survey.domain.statistics.service;

import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import com.woongjin.survey.domain.statistics.domain.statresult.ChoiceStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.QuestionStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.RankingStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.ScaleStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.SubjectiveStatResult;
import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionMetaDto;
import com.woongjin.survey.domain.statistics.dto.QuestionStatItemResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionStatisticsListResponse;
import com.woongjin.survey.domain.statistics.dto.QuestionStatisticsResponse;
import com.woongjin.survey.domain.statistics.dto.RespondentAnswerDto;
import com.woongjin.survey.domain.statistics.dto.ResponseListResponse;
import com.woongjin.survey.domain.statistics.dto.StatisticsSummaryResponse;
import com.woongjin.survey.domain.statistics.dto.projection.SurveySummaryProjection;
import com.woongjin.survey.domain.statistics.repository.QuestionStatRepository;
import com.woongjin.survey.domain.statistics.repository.StatisticsRepository;
import com.woongjin.survey.domain.survey.domain.Question;
import com.woongjin.survey.domain.survey.domain.QuestionItem;
import com.woongjin.survey.domain.survey.domain.Survey;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsQueryService {

    private static final int RESPONSE_PREVIEW_LIMIT = 50;

    private final StatisticsRepository statisticsRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionStatRepository questionStatRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;

    @Transactional(readOnly = true)
    public StatisticsSummaryResponse getSummary(Long surveyId) {
        SurveySummaryProjection p = statisticsRepository.findSummaryBySurveyId(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));
        return StatisticsSummaryResponse.from(p);
    }

    @Transactional(readOnly = true)
    public List<DeptResponseRateResponse> getDeptResponseRates(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));
        boolean isDeadlineToday = LocalDate.now().isEqual(survey.getEndDate().toLocalDate());
        return statisticsRepository.findDeptResponseRates(surveyId).stream()
                .map(p -> DeptResponseRateResponse.from(p, isDeadlineToday))
                .sorted(Comparator.comparingDouble(DeptResponseRateResponse::responseRate).reversed())
                .toList();
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

    /**
     * 문항별 응답현황 조회 — 통계 페이지 두 번째 탭.
     *
     * [흐름]
     *  1) 통계 테이블에서 집계 raw 데이터 조회 (문항별 1행)
     *  2) 문항/선택지 텍스트 조회 (정렬 순서대로)
     *  3) 두 결과를 questionId 로 매칭하여 화면용 DTO 빌드
     *
     * [반환]
     *  - 배치가 안 돌았으면 questions = 빈 리스트, aggregatedAt = null
     *  - 배치 돌았으면 문항 정렬 순서대로 통계 반환 + 마지막 집계 시각
     */
    @Transactional(readOnly = true)
    public QuestionStatisticsListResponse getQuestionStatistics(Long surveyId) {

        if (!surveyRepository.existsById(surveyId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }

        List<QuestionStat> stats = questionStatRepository.findBySurveyId(surveyId);
        if (stats.isEmpty()) {
            return new QuestionStatisticsListResponse(null, List.of());
        }

        // 문항 메타 + 선택지 텍스트 (sortOrder 정렬)
        List<Question> questions = surveyQuestionRepository
                .findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(surveyId);

        Map<Long, QuestionStat> statByQuestionId = stats.stream()
                .collect(Collectors.toMap(QuestionStat::getQuestionId, s -> s));

        List<QuestionStatisticsResponse> result = new ArrayList<>(questions.size());
        for (Question q : questions) {
            QuestionStat stat = statByQuestionId.get(q.getId());
            if (stat == null) continue;     // 통계가 아직 없는 문항은 스킵
            result.add(toResponse(q, stat));
        }

        return new QuestionStatisticsListResponse(stats.get(0).getAggregatedAt(), result);
    }

    /** 문항 1개 + 통계 1개 → 화면용 DTO */
    private QuestionStatisticsResponse toResponse(Question question, QuestionStat stat) {
        QuestionStatResult data = stat.getStatData();
        int total = stat.getTotalResponseCount();

        List<QuestionStatItemResponse> items;
        Double average = null;
        List<String> sampleTexts = null;

        if (data instanceof ChoiceStatResult c) {
            items = buildChoiceItems(c, question.getItems(), total);
        } else if (data instanceof ScaleStatResult s) {
            items = buildScaleItems(s, question.getItems(), total);
            average = s.average();
        } else if (data instanceof RankingStatResult r) {
            items = buildRankingItems(r, question.getItems(), total);
        } else if (data instanceof SubjectiveStatResult sub) {
            items = List.of();
            sampleTexts = sub.sampleTexts();
        } else {
            throw new IllegalStateException("Unknown stat result: " + data.getClass());
        }

        return new QuestionStatisticsResponse(
                question.getId(),
                question.getQuestionType(),
                question.getQuestionName(),
                total,
                items,
                average,
                sampleTexts
        );
    }

    /** 선택형 — 선택지 텍스트 정렬 순서로, count 와 percentage 포함 */
    private List<QuestionStatItemResponse> buildChoiceItems(
            ChoiceStatResult data, List<QuestionItem> options, int total) {
        return options.stream()
                .filter(opt -> opt.getDeletedAt() == null)
                .map(opt -> {
                    int count = data.itemCounts().getOrDefault(opt.getId(), 0);
                    return new QuestionStatItemResponse(opt.getItemName(), count, percentage(count, total));
                })
                .toList();
    }

    /**
     * 척도형 — 선택지(options) 정렬 순서를 그대로 사용.
     *  - 응답이 0인 옵션도 0% 카드로 표시되도록 options 기준 순회
     *  - scaleValue 는 1..N 점수이며 options[scaleValue - 1] 와 매핑
     *  - label 은 옵션 텍스트 ("매우 만족" 등)
     */
    private List<QuestionStatItemResponse> buildScaleItems(
            ScaleStatResult data, List<QuestionItem> options, int total) {
        List<QuestionItem> active = options.stream()
                .filter(opt -> opt.getDeletedAt() == null)
                .toList();

        List<QuestionStatItemResponse> result = new ArrayList<>(active.size());
        for (int i = 0; i < active.size(); i++) {
            int score = i + 1;
            int count = data.valueCounts().getOrDefault(score, 0);
            result.add(new QuestionStatItemResponse(
                    active.get(i).getItemName(),
                    count,
                    percentage(count, total)));
        }
        return result;
    }

    /** 순위형 — 1순위 카운트만 추출하여 단순 막대로 표시 */
    private List<QuestionStatItemResponse> buildRankingItems(
            RankingStatResult data, List<QuestionItem> options, int total) {
        return options.stream()
                .filter(opt -> opt.getDeletedAt() == null)
                .map(opt -> {
                    Map<Integer, Integer> ranks = data.rankCounts().getOrDefault(opt.getId(), Map.of());
                    int firstPlaceCount = ranks.getOrDefault(1, 0);
                    return new QuestionStatItemResponse(
                            opt.getItemName(), firstPlaceCount, percentage(firstPlaceCount, total));
                })
                .toList();
    }

    private double percentage(int count, int total) {
        if (total == 0) return 0.0;
        return Math.round((double) count / total * 1000) / 10.0;   // 소수 1자리
    }
}

