package com.woongjin.survey.domain.statistics.batch.processor;

import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import com.woongjin.survey.domain.statistics.domain.statresult.ChoiceStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.QuestionStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.RankingStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.ScaleStatResult;
import com.woongjin.survey.domain.statistics.domain.statresult.SubjectiveStatResult;
import com.woongjin.survey.domain.survey.domain.Answer;
import com.woongjin.survey.domain.survey.domain.Question;
import com.woongjin.survey.domain.survey.domain.enums.QuestionType;
import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;
import com.woongjin.survey.domain.survey.repository.SurveyQuestionRepository;
import com.woongjin.survey.domain.survey.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 배치 Processor — surveyId 하나를 받아 List<QuestionStat> 으로 변환.
 *
 * [흐름]
 *  1) 해당 설문의 문항 메타 조회
 *  2) 해당 설문의 응답 전체 로드
 *  3) 응답 JSON 을 펼쳐 문항 ID 별로 그룹핑
 *  4) 문항별로 타입에 맞는 집계 수행 → QuestionStatResult 생성
 *  5) QuestionStat 엔티티로 조립해 List 로 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsItemProcessor implements ItemProcessor<Long, List<QuestionStat>> {

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    @Override
    public List<QuestionStat> process(Long surveyId) {
        log.info("[stat-batch] 설문 집계 시작 surveyId={}", surveyId);

        List<Question> questions = surveyQuestionRepository
                .findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(surveyId);

        if (questions.isEmpty()) {
            log.info("[stat-batch] 문항 없음 → 건너뜀 surveyId={}", surveyId);
            return List.of();
        }

        List<Answer> responses = surveyResponseRepository.findBySurveyId(surveyId);
        if (responses.isEmpty()) {
            log.info("[stat-batch] 응답 없음 → 건너뜀 surveyId={}", surveyId);
            return List.of();
        }

        Map<Long, List<SurveyAnswerDto>> answersByQuestion = groupByQuestionId(responses);

        LocalDateTime now = LocalDateTime.now();
        List<QuestionStat> result = new ArrayList<>(questions.size());
        for (Question q : questions) {
            List<SurveyAnswerDto> answers = answersByQuestion.getOrDefault(q.getId(), List.of());
            result.add(QuestionStat.from(q, answers.size(), aggregate(q.getQuestionType(), answers), now));
        }

        log.info("[stat-batch] 설문 집계 완료 surveyId={}, 응답수={}, 문항수={}",
                surveyId, responses.size(), result.size());
        return result;
    }

    private QuestionStatResult aggregate(QuestionType type, List<SurveyAnswerDto> answers) {
        return switch (type) {
            case SINGLE_CHOICE, MULTIPLE_CHOICE -> aggregateChoice(answers);
            case SCALE                          -> aggregateScale(answers);
            case SUBJECTIVE                     -> aggregateSubjective(answers);
            case RANKING                        -> aggregateRanking(answers);
        };
    }

    // 항목별 선택 횟수 카운트
    // 예) [{selected:[1,2]}, {selected:[1]}] → {1:2, 2:1}
    private ChoiceStatResult aggregateChoice(List<SurveyAnswerDto> answers) {
        Map<Long, Integer> itemCounts = new HashMap<>();
        for (SurveyAnswerDto a : answers) {
            List<Long> selected = a.getSelectedItemIds();
            if (selected == null) continue;
            for (Long itemId : selected) {
                itemCounts.merge(itemId, 1, Integer::sum);
            }
        }
        return new ChoiceStatResult(itemCounts);
    }

    // 점수별 카운트 + 평균(소수 둘째 자리)
    // 예) [3, 5, 4] → {3:1, 4:1, 5:1}, avg=4.0
    private ScaleStatResult aggregateScale(List<SurveyAnswerDto> answers) {
        Map<Integer, Integer> valueCounts = new HashMap<>();
        long sum = 0;
        int validCount = 0;

        for (SurveyAnswerDto a : answers) {
            Integer value = a.getScaleValue();
            if (value == null) continue;
            valueCounts.merge(value, 1, Integer::sum);
            sum += value;
            validCount++;
        }

        double average = (validCount == 0) ? 0.0
                : Math.round((double) sum / validCount * 100) / 100.0;

        return new ScaleStatResult(valueCounts, average);
    }

    // 비어있지 않은 응답 수만 집계 (텍스트 내용은 저장하지 않음)
    // 예) ["좋아요", "", "보통"] → answered=2
    private SubjectiveStatResult aggregateSubjective(List<SurveyAnswerDto> answers) {
        int answered = 0;
        for (SurveyAnswerDto a : answers) {
            String text = a.getTextAnswer();
            if (text != null && !text.isBlank()) {
                answered++;
            }
        }
        return new SubjectiveStatResult(answered);
    }

    // 항목별 순위별 카운트 (배열 index 0 = 1순위)
    // 예) [{ranked:[A,B]}, {ranked:[B,A]}] → {A:{1위:1,2위:1}, B:{1위:1,2위:1}}
    private RankingStatResult aggregateRanking(List<SurveyAnswerDto> answers) {
        Map<Long, Map<Integer, Integer>> rankCounts = new HashMap<>();
        for (SurveyAnswerDto a : answers) {
            List<Long> ranked = a.getRankedItemIds();
            if (ranked == null) continue;
            for (int i = 0; i < ranked.size(); i++) {
                Long itemId = ranked.get(i);
                int rank = i + 1;
                rankCounts
                        .computeIfAbsent(itemId, k -> new HashMap<>())
                        .merge(rank, 1, Integer::sum);
            }
        }
        return new RankingStatResult(rankCounts);
    }

    /**
     * Answer.answers (문항별 답변 배열) 를 펼쳐
     * {questionId → 그 문항에 대한 모든 응답자의 답변 리스트} 형태로 묶음.
     */
    private Map<Long, List<SurveyAnswerDto>> groupByQuestionId(List<Answer> responses) {
        Map<Long, List<SurveyAnswerDto>> map = new HashMap<>();
        for (Answer r : responses) {
            List<SurveyAnswerDto> answers = r.getAnswers();
            if (answers == null) continue;
            for (SurveyAnswerDto a : answers) {
                map.computeIfAbsent(a.getQuestionId(), k -> new ArrayList<>()).add(a);
            }
        }
        return map;
    }
}
