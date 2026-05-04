package com.woongjin.survey.domain.statistics.batch.reader;

import com.woongjin.survey.domain.survey.domain.enums.SurveyStatus;
import com.woongjin.survey.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통계 배치 Reader — 진행중 설문 ID 하나씩 흘려보냄.
 *
 * [동작]
 * - read() 가 호출될 때마다 ID 하나씩 반환
 * - 더 이상 없으면 null 반환 → Step 종료
 *
 * [구현 메모]
 * - ListItemReader 로 위임. Step 시작 시점에 한 번에 ID 목록 로드.
 * - 진행중 설문 수가 수십 개 수준이라 메모리 부담 없음.
 *   (수만 개 단위라면 JpaPagingItemReader 등으로 페이징 필요)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSurveyIdReader {

    private final SurveyRepository surveyRepository;

    /**
     * Step 실행 시점에 진행중 설문 ID 목록을 조회해 ItemReader 로 감싸 반환.
     * Job 가 실행될 때마다 호출되어 매번 최신 목록을 가져온다.
     */
    public ItemReader<Long> reader() {
        List<Long> ids = surveyRepository.findActiveSurveyIds(
                SurveyStatus.APPROVED,
                LocalDateTime.now()
        );
        log.info("[stat-batch] 집계 대상 설문 수: {}", ids.size());
        return new ListItemReader<>(ids);
    }
}
