package com.woongjin.survey.domain.statistics.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 설문 통계 집계 배치 스케줄러.
 *
 * [실행 시점]
 *  매일 오전 8시 (서버 시간 기준)
 *
 * [JobParameters]
 *  Spring Batch 는 같은 파라미터로 성공한 Job 을 재실행하지 않으므로
 *  매번 timestamp 를 추가해 새 실행으로 인식되게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job questionStatisticsAggregateJob;

    @Scheduled(cron = "0 0 8 * * *")   // 매일 오전 8시
    public void run() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("[stat-scheduler] 배치 시작");
            jobLauncher.run(questionStatisticsAggregateJob, params);
            log.info("[stat-scheduler] 배치 완료");

        } catch (Exception e) {
            log.error("[stat-scheduler] 배치 실행 실패", e);
        }
    }
}
