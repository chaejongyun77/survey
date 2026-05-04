package com.woongjin.survey.domain.statistics.batch;

import com.woongjin.survey.domain.statistics.batch.processor.StatisticsItemProcessor;
import com.woongjin.survey.domain.statistics.batch.reader.ActiveSurveyIdReader;
import com.woongjin.survey.domain.statistics.batch.writer.StatisticsItemWriter;
import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 설문 통계 집계 배치 Job/Step 정의.
 *
 * [구조]
 *  Job  : questionStatisticsAggregateJob
 *   └ Step: aggregateStep  (chunk size = 100)
 *        ├ Reader    : ActiveSurveyIdReader#reader()  (Long surveyId 흘려보냄)
 *        ├ Processor : StatisticsItemProcessor       (Long → List&lt;QuestionStat&gt;)
 *        └ Writer    : StatisticsItemWriter          (UPSERT)
 *
 * [Chunk 의미]
 *  - chunk(100) = "Processor 결과 100건이 모이면 Writer 호출 + 트랜잭션 커밋"
 *  - 여기서 한 건 = 한 설문의 통계 (List&lt;QuestionStat&gt;)
 *  - 진행중 설문이 100개 미만이면 한 번에 끝남
 */
@Configuration
@RequiredArgsConstructor
public class StatisticsBatchConfig {

    private static final int CHUNK_SIZE = 100;

    private final ActiveSurveyIdReader     surveyIdReaderProvider;
    private final StatisticsItemProcessor  itemProcessor;
    private final StatisticsItemWriter     itemWriter;

    @Bean
    public Job questionStatisticsAggregateJob(JobRepository jobRepository,
                                              Step aggregateStep) {
        return new JobBuilder("questionStatisticsAggregateJob", jobRepository)
                .start(aggregateStep)
                .build();
    }

    @Bean
    public Step aggregateStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager) {
        return new StepBuilder("aggregateStep", jobRepository)
                .<Long, List<QuestionStat>>chunk(CHUNK_SIZE, transactionManager)
                .reader(surveyIdReaderProvider.reader())
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }
}
