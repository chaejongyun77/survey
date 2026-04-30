package com.woongjin.survey.domain.statistics.dto;

import java.util.List;

/**
 * 응답자별 문항답변 조회 응답 DTO (최상위)
 *
 * [화면 매핑]
 * - 응답자별 문항답변 테이블 전체
 *   · questions  : 동적 컬럼 헤더 + 항목 라벨 매핑 데이터
 *   · responses  : 테이블 row 목록 (최근 30건 미리보기)
 *
 * [설계 메모]
 * - 문항은 limit 없이 전체 내려보냄 (좌우 스크롤로 전체 표시)
 * - 응답자는 최근 30건으로 제한, 전체 응답은 엑셀 다운로드 API 로 별도 제공
 */
public record ResponseListResponse(
        List<QuestionMetaDto> questions,        // 문항 메타 (헤더 + 라벨 매핑)
        List<RespondentAnswerDto> responses     // 응답자 목록 (최근 30건)
) {
}
