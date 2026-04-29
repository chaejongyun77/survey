package com.woongjin.survey.domain.statistics.dto;

import com.woongjin.survey.domain.survey.dto.submit.SurveyAnswerDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 응답자 1명의 응답 데이터 — 통계 응답용
 *
 * [화면 매핑]
 * - 응답자별 문항답변 테이블의 한 행(row)
 *   → 응답자 · 부서 · 문항1 · 문항2 · ... · 문항N
 *
 * [설계 메모]
 * - answers 필드는 SurveyAnswerDto 를 그대로 재사용
 *   → 응답 저장 시 사용한 DTO 와 동일 (Answer.answers 의 타입)
 *   → JSON ↔ Java 변환 코드 추가 작성 불필요 (Hibernate 6 JSON 자동 매핑)
 * - 미응답 문항은 answers 리스트에 아예 포함되지 않음
 *   → 프론트에서 questions 순회 시 answers 에 없는 questionId 는 빈 셀 처리
 */
public record RespondentAnswerDto(
        String empName,                  // 홍석균
        String deptName,                 // CIT LAB팀
        LocalDateTime submittedAt,       // 응답 일시 (FRST_CRTN_DT)
        List<SurveyAnswerDto> answers    // 응답 JSON 그대로
) {
}
