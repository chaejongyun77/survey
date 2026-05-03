package com.woongjin.survey.domain.statistics.batch.processor;

import com.woongjin.survey.domain.statistics.aggregator.QuestionStatAggregator;
import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import com.woongjin.survey.domain.statistics.domain.statdata.QuestionStatData;
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
 *  4) 문항별로 타입에 맞는 Aggregator 호출 → QuestionStatData 생성
 *  5) QuestionStat 엔티티로 조립해 List 로 반환
 *
 * [확장 포인트]
 *  - 새 문항 타입이 추가되면 새 Aggregator 만 추가하면 됨
 *  - Spring 이 List<QuestionStatAggregator> 로 모든 구현체 자동 주입
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsItemProcessor implements ItemProcessor<Long, List<QuestionStat>> {

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final List<QuestionStatAggregator> aggregators;

    @Override
    public List<QuestionStat> process(Long surveyId) {
        log.info("[stat-batch] 설문 집계 시작 surveyId={}", surveyId);

        // 1) 문항 메타 (타입 정보 필요)
        List<Question> questions = surveyQuestionRepository
                .findBySurveyIdAndDeletedAtIsNullOrderBySortOrderAsc(surveyId);

        if (questions.isEmpty()) {
            log.info("[stat-batch] 문항 없음 → 건너뜀 surveyId={}", surveyId);
            return List.of();
        }

        // 2) 응답 전체 로드
        List<Answer> responses = surveyResponseRepository.findBySurveyId(surveyId);

        // 3) 응답 JSON 펼쳐서 문항 ID 별로 그룹핑
        Map<Long, List<SurveyAnswerDto>> answersByQuestion = groupByQuestionId(responses);

        // 4) 문항별 집계
        LocalDateTime now = LocalDateTime.now();
        List<QuestionStat> result = new ArrayList<>(questions.size());
        for (Question q : questions) {
            List<SurveyAnswerDto> answers = answersByQuestion.getOrDefault(q.getId(), List.of());
            result.add(aggregateOne(q, answers, now));
        }

        log.info("[stat-batch] 설문 집계 완료 surveyId={}, 응답수={}, 문항수={}",
                surveyId, responses.size(), result.size());
        return result;
    }

    /** 한 문항에 대한 집계 — Aggregator 매칭 + QuestionStat 조립 */
    private QuestionStat aggregateOne(Question question, List<SurveyAnswerDto> answers, LocalDateTime now) {
        QuestionType type = question.getQuestionType();
        QuestionStatAggregator aggregator = aggregators.stream()
                .filter(a -> a.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "문항 타입에 대응하는 Aggregator 없음: " + type));

        QuestionStatData data = aggregator.aggregate(answers);

        return QuestionStat.builder()
                .surveyId(question.getSurveyId())
                .questionId(question.getId())
                .questionType(type)
                .totalResponseCount(answers.size())
                .statData(data)
                .aggregatedAt(now)
                .build();
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
