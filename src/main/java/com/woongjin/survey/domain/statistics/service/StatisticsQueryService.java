package com.woongjin.survey.domain.statistics.service;

import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import com.woongjin.survey.domain.statistics.domain.statresult.ChoiceStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.QuestionStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.RankingStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.ScaleStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.SubjectiveStatResult;
import com.woongjin.survey.domain.statistics.dto.DeptResponseRateResponse;
import com.woongjin.survey.domain.statistics.dto.projection.DeptResponseRateProjection;
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
import com.woongjin.survey.domain.survey.domain.QuestionBranch;
import com.woongjin.survey.domain.survey.domain.QuestionItem;
import com.woongjin.survey.domain.survey.repository.QuestionBranchRepository;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import com.woongjin.survey.global.exception.BusinessException;
import com.woongjin.survey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final QuestionBranchRepository questionBranchRepository;

    @Transactional(readOnly = true)
    public StatisticsSummaryResponse getSummary(Long surveyId) {
        SurveySummaryProjection p = statisticsRepository.findSummaryBySurveyId(surveyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SURVEY_NOT_FOUND));
        return StatisticsSummaryResponse.from(p);
    }

    /**
     * 조직별 응답률 — 상위(LVL=1) 부서 아래 하위(LVL=2) 부서를 children 으로 묶어 트리 반환.
     *
     * [조립 규칙]
     *  - 모든 행을 "LVL=1 루트 ID" 기준으로 그룹핑:
     *      · parentDeptId 가 있으면 → 그 부모 ID 사용 (LVL=2 자식 행)
     *      · parentDeptId 가 null 이면 → 자기 자신 ID 사용 (LVL=1 직속 행)
     *    → 같은 LVL=1 트리에 속하는 행들이 한 그룹으로 모임
     *  - 그룹 안의 self 행(deptId == 루트 ID)은 합산에 포함하되 children 리스트에는 안 넣음
     *    → "LVL=1 부서에 직원 직속 + 자식 부서도 있음" 인 경우 부모 노드가 두 번 표시되는 버그 회피
     *  - 자식 없이 self 만 있으면 leaf 단독 노드
     *  - 부모는 응답률 내림차순, children 도 내부에서 응답률 내림차순
     */
    @Transactional(readOnly = true)
    public List<DeptResponseRateResponse> getDeptResponseRates(Long surveyId) {
        if (!surveyRepository.existsById(surveyId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }

        List<DeptResponseRateProjection> leafProjections = statisticsRepository.findDeptResponseRates(surveyId);

        Map<Long, List<DeptResponseRateProjection>> rowsByRootId = leafProjections.stream()
                .collect(Collectors.groupingBy(p ->
                        p.parentDeptId() != null ? p.parentDeptId() : p.deptId()));

        List<DeptResponseRateResponse> rootNodes = new ArrayList<>();
        rowsByRootId.forEach((rootDeptId, group) ->
                rootNodes.add(buildRootNode(rootDeptId, group)));

        rootNodes.sort(Comparator.comparingDouble(DeptResponseRateResponse::responseRate).reversed());
        return rootNodes;
    }

    /**
     * 한 LVL=1 트리(self 행 + 자식 행) → 루트 노드 1개로 조립.
     *
     * - 자식 없음 → self 단독 leaf 반환
     * - 자식 있음 → 합산 부모 노드 반환. self 직속 행이 있으면 합산에는 포함하되
     *   children 리스트에는 추가하지 않음(중복 표시 방지)
     */
    private DeptResponseRateResponse buildRootNode(Long rootDeptId,
                                                    List<DeptResponseRateProjection> group) {
        DeptResponseRateProjection selfRow = group.stream()
                .filter(p -> p.deptId().equals(rootDeptId))
                .findFirst()
                .orElse(null);

        List<DeptResponseRateProjection> childRows = group.stream()
                .filter(p -> !p.deptId().equals(rootDeptId))
                .toList();

        // 자식이 없으면 self 단독 leaf
        if (childRows.isEmpty()) {
            return DeptResponseRateResponse.leaf(selfRow);
        }

        // 자식 있음 → 부모 노드 (self 직속이 있으면 합산에 흡수)
        int totalTargetCount    = group.stream().mapToInt(p -> (int) p.targetCount()).sum();
        int totalRespondedCount = group.stream().mapToInt(p -> (int) p.respondedCount()).sum();

        // 부모 이름: self 행에서 우선 추출, 없으면 자식의 parentDeptName 사용
        String rootDeptName = selfRow != null
                ? selfRow.deptName()
                : childRows.get(0).parentDeptName();

        List<DeptResponseRateResponse> childNodes = childRows.stream()
                .map(DeptResponseRateResponse::leaf)
                .sorted(Comparator.comparingDouble(DeptResponseRateResponse::responseRate).reversed())
                .toList();

        return DeptResponseRateResponse.parent(
                rootDeptId, rootDeptName, totalTargetCount, totalRespondedCount, childNodes);
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
     * 응답자별 문항답변 — 전체 조회 (엑셀 다운로드용)
     *
     * - questions : 동적 컬럼 헤더 + 라벨 매핑 데이터 (전체)
     * - responses : limit 없이 전체 응답 반환
     */
    @Transactional(readOnly = true)
    public ResponseListResponse getResponseListForExcel(Long surveyId) {

        if (!surveyRepository.existsById(surveyId)) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }

        List<QuestionMetaDto> questions = statisticsRepository.findQuestionsWithItems(surveyId);
        List<RespondentAnswerDto> responses = statisticsRepository.findRecentResponses(surveyId, Integer.MAX_VALUE);
        int totalCount = responses.size();

        return new ResponseListResponse(questions, responses, totalCount, totalCount);
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

        List<Long> questionIds = questions.stream().map(Question::getId).toList();

        // 분기 관계 조회 — 자식 문항을 부모 바로 뒤에 배치하기 위함
        List<QuestionBranch> branches = questionIds.isEmpty()
                ? List.of()
                : questionBranchRepository.findByParentQuestionIdIn(questionIds);

        // ID → Question 조회용
        Map<Long, Question> questionById = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // parentId → 자식 Question (1:1)
        Map<Long, Question> childByParentId = branches.stream()
                .collect(Collectors.toMap(
                        QuestionBranch::getParentQuestionId,
                        b -> questionById.get(b.getChildQuestionId())
                ));

        // 자식 문항 ID 집합 — 메인 루프에서 스킵 판단용
        Set<Long> childIds = branches.stream()
                .map(QuestionBranch::getChildQuestionId)
                .collect(Collectors.toSet());

        Map<Long, QuestionStat> statByQuestionId = stats.stream()
                .collect(Collectors.toMap(QuestionStat::getQuestionId, s -> s));

        List<QuestionStatisticsResponse> result = new ArrayList<>(questions.size());
        for (Question q : questions) {
            if (childIds.contains(q.getId())) continue;  // 자식은 부모 처리 시 삽입

            QuestionStat stat = statByQuestionId.get(q.getId());
            if (stat != null) result.add(toResponse(q, stat));

            // 이 부모의 자식 문항을 바로 뒤에 삽입
            Question child = childByParentId.get(q.getId());
            if (child != null) {
                QuestionStat childStat = statByQuestionId.get(child.getId());
                if (childStat != null) result.add(toResponse(child, childStat));
            }
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

    /** 순위형 — 가중 점수(1순위=N점, 2순위=N-1점, ...) 기준 상대 비율로 표시 */
    private List<QuestionStatItemResponse> buildRankingItems(
            RankingStatResult data, List<QuestionItem> options, int total) {
        List<QuestionItem> active = options.stream()
                .filter(opt -> opt.getDeletedAt() == null)
                .toList();
        int N = active.size();

        int[] scores = new int[active.size()];
        for (int i = 0; i < active.size(); i++) {
            Map<Integer, Integer> ranks = data.rankCounts().getOrDefault(active.get(i).getId(), Map.of());
            for (Map.Entry<Integer, Integer> e : ranks.entrySet()) {
                scores[i] += e.getValue() * (N - e.getKey() + 1);
            }
        }

        int maxScore = 0;
        for (int s : scores) if (s > maxScore) maxScore = s;

        List<QuestionStatItemResponse> result = new ArrayList<>(active.size());
        for (int i = 0; i < active.size(); i++) {
            double pct = maxScore == 0 ? 0.0 : Math.round(scores[i] * 1000.0 / maxScore) / 10.0;
            result.add(new QuestionStatItemResponse(active.get(i).getItemName(), scores[i], pct));
        }
        return result;
    }

    private double percentage(int count, int total) {
        if (total == 0) return 0.0;
        return Math.round((double) count / total * 1000) / 10.0;   // 소수 1자리
    }
}

