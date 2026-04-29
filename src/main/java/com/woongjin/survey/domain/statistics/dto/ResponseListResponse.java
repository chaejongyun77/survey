package com.woongjin.survey.domain.statistics.dto;

import java.util.List;

/**
 * 응답자별 문항답변 조회 응답 DTO (최상위)
 *
 * [화면 매핑]
 * - 응답자별 문항답변 테이블 전체
 *   · questions       : 동적 컬럼 헤더 + 항목 라벨 매핑 데이터
 *   · responses       : 테이블 row 목록 (최근 N건 미리보기)
 *   · totalCount      : "총 487건 중 50건 미리보기" 안내 표시용
 *   · previewLimit    : 클라이언트에 limit 값 전달 (안내 메시지 일관성 유지)
 *
 * [설계 메모]
 * - 화면에선 limit 으로 자른 미리보기만 표시
 *   → 전체 응답은 엑셀 다운로드 API 로 별도 제공
 * - questions 는 항상 전체 내려보냄 (limit 무관)
 *   → 화면 컬럼 헤더는 응답 수와 상관없이 고정이어야 하므로
 */
public record ResponseListResponse(
        List<QuestionMetaDto> questions,        // 문항 메타 (헤더 + 라벨 매핑)
        List<RespondentAnswerDto> responses,    // 응답자 목록 (최근 N건)
        int totalCount,                         // 전체 응답 수 (안내용)
        int previewLimit                        // 미리보기 limit 값 (예: 50)
) {
}
