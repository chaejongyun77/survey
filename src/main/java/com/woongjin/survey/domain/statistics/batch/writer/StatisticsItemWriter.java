package com.woongjin.survey.domain.statistics.batch.writer;

import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import com.woongjin.survey.domain.statistics.repository.QuestionStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 통계 배치 Writer — 한 설문분 통계를 UPSERT.
 *
 * [입력]
 *  Chunk&lt;List&lt;QuestionStat&gt;&gt;
 *   = "여러 설문의 통계 묶음" (chunk 사이즈 만큼 누적된 List들)
 *
 * [UPSERT 전략]
 *  같은 설문은 통째로 갈아끼우는 것이 가장 단순.
 *   1) deleteAllBySurveyId → 기존 행 일괄 삭제
 *   2) saveAll              → 새 통계 INSERT
 *  Step 의 트랜잭션 안에서 수행되므로 중간 빈 상태가 외부에 노출되지 않음.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsItemWriter implements ItemWriter<List<QuestionStat>> {

    private final QuestionStatRepository statRepository;

    @Override
    public void write(Chunk<? extends List<QuestionStat>> chunk) {
        for (List<QuestionStat> stats : chunk) {
            if (stats.isEmpty()) continue;

            Long surveyId = stats.get(0).getSurveyId();
            int deleted = statRepository.deleteAllBySurveyId(surveyId);
            statRepository.saveAll(stats);

            log.info("[stat-batch] UPSERT 완료 surveyId={}, 삭제={}, 신규={}",
                    surveyId, deleted, stats.size());
        }
    }
}
