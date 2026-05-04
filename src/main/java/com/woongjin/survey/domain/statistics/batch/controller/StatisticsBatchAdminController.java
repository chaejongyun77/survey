package com.woongjin.survey.domain.statistics.batch.controller;

import com.woongjin.survey.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 설문 통계 집계 배치 — 수동 실행용 관리자 컨트롤러.
 *
 * [용도]
 *  1) 개발/테스트 단계에서 30분 스케줄을 기다리지 않고 즉시 검증
 *  2) 운영 중 장애 복구 후 수동 재실행
 *
 * [JobParameters 함정]
 *  Spring Batch 는 같은 파라미터로 한 번 성공한 Job 을 다시 실행하지 않는다.
 *  → 매 호출마다 timestamp 를 파라미터로 넣어 새 실행으로 인식되게 한다.
 *
 * [동기 실행]
 *  jobLauncher.run() 은 기본 동기. Job 끝까지 대기 후 응답.
 *  검증 목적이라 응답에 결과(상태/소요시간)를 담아 반환한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/v1/admin/batch/statistics")
@RequiredArgsConstructor
public class StatisticsBatchAdminController {

    private final JobLauncher jobLauncher;
    private final Job questionStatisticsAggregateJob;

    @PostMapping("/run")
    public ApiResponse<Map<String, Object>> runManually() throws Exception {
        long startedAt = System.currentTimeMillis();

        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", startedAt)             // ← 매번 다른 값으로 새 실행 보장
                .toJobParameters();

        log.info("[stat-batch] 수동 실행 시작 timestamp={}", startedAt);
        JobExecution execution = jobLauncher.run(questionStatisticsAggregateJob, params);

        long elapsedMs = System.currentTimeMillis() - startedAt;

        Map<String, Object> result = Map.of(
                "jobExecutionId", execution.getId(),
                "status",         execution.getStatus().name(),
                "exitCode",       execution.getExitStatus().getExitCode(),
                "elapsedMs",      elapsedMs
        );

        log.info("[stat-batch] 수동 실행 종료 status={}, elapsedMs={}",
                execution.getStatus(), elapsedMs);

        return ApiResponse.success("통계 집계 배치 실행 완료", result);
    }
}
