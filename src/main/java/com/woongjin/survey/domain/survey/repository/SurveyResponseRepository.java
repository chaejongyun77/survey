package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<Answer, Long> {

    /**
     * 중복 응답 여부 확인
     * - SVY_ID + EMP_ID unique 제약 조건과 대응
     */
    boolean existsBySurveyIdAndEmpId(Long surveyId, Long empId);

    /**
     * 특정 설문의 모든 응답 조회 — 통계 배치 집계용
     * - JSON 컬럼(QST_ANSWR)을 포함해 모두 메모리에 로드
     * - 한 설문 단위 호출이라 1만 건 미만의 응답에서는 부담 없음
     */
    List<Answer> findBySurveyId(Long surveyId);

    /**
     * 특정 설문의 특정 문항 주관식 텍스트 조회 — 토글 시 on-demand 조회
     * JSON_TABLE로 QST_ANSWR 배열에서 해당 questionId의 textAnswer만 추출
     * 예) limit=30이면 비어있지 않은 답변 최대 30건 반환
     */
    @Query(value = """
            SELECT jt.text_answer
            FROM svy_rspn_tb r
            JOIN JSON_TABLE(r.QST_ANSWR, '$[*]' COLUMNS (
                question_id BIGINT        PATH '$.questionId',
                text_answer VARCHAR(500)  PATH '$.textAnswer'
            )) jt ON jt.question_id = :questionId
            WHERE r.SVY_ID = :surveyId
              AND jt.text_answer IS NOT NULL
              AND jt.text_answer <> ''
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findSubjectiveTextAnswers(
            @Param("surveyId")    Long surveyId,
            @Param("questionId")  Long questionId,
            @Param("limit")       int limit);
}
