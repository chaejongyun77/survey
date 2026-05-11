package com.woongjin.survey.domain.statistics.batch.controller;

import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 설문 통계 집계 배치 — 수동 실행용 관리자 컨트롤러.
 *
 * [JobParameters 함정]
 *  Spring Batch 는 같은 파라미터로 한 번 성공한 Job 을 다시 실행하지 않는다.
 *  → 매 호출마다 timestamp 를 파라미터로 넣어 새 실행으로 인식되게 한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/v1/admin/batch/statistics")
@RequiredArgsConstructor
public class StatisticsBatchAdminController {

    private final JobLauncher jobLauncher;
    private final Job questionStatisticsAggregateJob;

    @PostMapping("/run")
    public ApiResponse<Void> runManually() throws Exception {
        long startedAt = System.currentTimeMillis();

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", startedAt)
                .toJobParameters();

        log.info("[stat-batch] 수동 실행 시작 timestamp={}", startedAt);
        jobLauncher.run(questionStatisticsAggregateJob, params);
        log.info("[stat-batch] 수동 실행 종료 elapsedMs={}", System.currentTimeMillis() - startedAt);

        return ApiResponse.success("통계 집계 배치 실행 완료", null);
    }
}
