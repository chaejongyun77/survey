package com.woongjin.survey.domain.statistics.batch;

import com.woongjin.survey.domain.statistics.batch.processor.StatisticsItemProcessor;
import com.woongjin.survey.domain.statistics.batch.writer.StatisticsItemWriter;
import com.woongjin.survey.domain.statistics.domain.QuestionStat;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 설문 통계 집계 배치 Job/Step 정의.
 *
 * [구조]
 *  Job  : questionStatisticsAggregateJob
 *   └ Step: aggregateStep  (chunk size = 100)
 *        ├ Reader    : 진행중 설문 ID 목록을 흘려보냄
 *        ├ Processor : StatisticsItemProcessor (Long → List&lt;QuestionStat&gt;)
 *        └ Writer    : StatisticsItemWriter    (UPSERT)
 *
 * [Chunk 의미]
 *  - chunk(100) = "Processor 결과 100건이 모이면 Writer 호출 + 트랜잭션 커밋"
 *  - 여기서 한 건 = 한 설문의 통계 (List&lt;QuestionStat&gt;)
 *  - 진행중 설문이 100개 미만이면 한 번에 끝남
 *
 * [Reader @StepScope]
 *  activeSurveyIdReader 는 @StepScope Bean 으로 분리.
 *  private 메서드로 두면 앱 기동 시점에 한 번만 실행되어 캐싱되므로
 *  2회차 실행부터 Reader 가 소진된 상태로 아무것도 처리되지 않는 문제가 생김.
 *  @StepScope 로 선언하면 Step 실행마다 새 인스턴스가 생성되어 매번 DB 를 다시 조회함.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsBatchConfig {

    private static final int CHUNK_SIZE = 100;

    private final StatisticsItemProcessor itemProcessor;
    private final StatisticsItemWriter    itemWriter;
    private final SurveyRepository        surveyRepository;

    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        launcher.afterPropertiesSet();
        return launcher;
    }

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
                .reader(activeSurveyIdReader())
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    /**
     * 진행중 설문 ID 목록을 한 번에 로드해 Reader 로 감싸 반환.
     * 진행중 설문 수가 수십 개 수준이라 메모리 부담 없음.
     * (수만 개 단위라면 JpaPagingItemReader 등으로 페이징 필요)
     */
    @Bean
    @StepScope
    public ItemReader<Long> activeSurveyIdReader() {
        List<Long> ids = surveyRepository.findActiveSurveyIds();
        log.info("[stat-batch] 집계 대상 설문 수: {}", ids.size());
        return new ListItemReader<>(ids);
    }
}
