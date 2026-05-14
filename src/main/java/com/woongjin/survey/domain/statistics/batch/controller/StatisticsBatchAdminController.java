package com.woongjin.survey.domain.statistics.batch.controller;

import com.woongjin.survey.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 설문 통계 집계 배치 — 수동 실행용 관리자 컨트롤러.
 *
 * [비동기 실행]
 *  asyncJobLauncher(SimpleAsyncTaskExecutor) 로 즉시 반환 후,
 *  클라이언트가 /status 를 폴링해 완료 여부를 확인한다.
 *
 * [JobParameters 함정]
 *  Spring Batch 는 같은 파라미터로 한 번 성공한 Job 을 다시 실행하지 않는다.
 *  → 매 호출마다 timestamp 를 파라미터로 넣어 새 실행으로 인식되게 한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/v1/admin/batch/statistics")
public class StatisticsBatchAdminController {

    private final JobLauncher asyncJobLauncher;
    private final Job questionStatisticsAggregateJob;
    private final JobExplorer jobExplorer;

    public StatisticsBatchAdminController(
            @Qualifier("asyncJobLauncher") JobLauncher asyncJobLauncher,
            Job questionStatisticsAggregateJob,
            JobExplorer jobExplorer) {
        this.asyncJobLauncher = asyncJobLauncher;
        this.questionStatisticsAggregateJob = questionStatisticsAggregateJob;
        this.jobExplorer = jobExplorer;
    }

    @PostMapping("/run")
    public ApiResponse<Void> runManually() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        log.info("[stat-batch] 수동 실행 시작");
        asyncJobLauncher.run(questionStatisticsAggregateJob, params);
        return ApiResponse.success("통계 집계 배치 시작됨");
    }

    @GetMapping("/status")
    public ApiResponse<String> status() {
        List<JobInstance> instances = jobExplorer.getJobInstances("questionStatisticsAggregateJob", 0, 1);
        if (instances.isEmpty()) return ApiResponse.success(null, "NONE");
        JobExecution exec = jobExplorer.getLastJobExecution(instances.get(0));
        if (exec == null) return ApiResponse.success(null, "NONE");
        return ApiResponse.success(null, exec.getStatus().name());
    }
}
