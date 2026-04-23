package com.woongjin.survey.domain.survey.service;

import com.woongjin.survey.domain.survey.dto.submit.AnswerDto;
import com.woongjin.survey.domain.survey.dto.submit.DraftRequest;
import com.woongjin.survey.domain.survey.infra.SurveyDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyDraftService {

    private final SurveyDraftRepository surveyDraftRepository;
    private final SurveyParticipationValidator participationValidator;

    /**
     * 임시저장
     * - 참여 가능 여부(기간·대상자·이미 제출) 검증 후 Redis 에 저장
     * - 빈 answers 허용 (부분 저장 지원)
     */
    public void saveDraft(Long surveyId, Long empId, DraftRequest request) {
        participationValidator.checkParticipate(surveyId, empId);
        surveyDraftRepository.save(empId, surveyId, request.getAnswers());
        log.info("[saveDraft] 완료: surveyId={}, empId={}, answers.size={}", surveyId, empId, request.getAnswers().size());
    }

    /**
     * 임시저장 조회
     * - 저장된 draft 없으면 Optional.empty() 반환
     */
    public Optional<List<AnswerDto>> getDraft(Long surveyId, Long empId) {
        return surveyDraftRepository.find(empId, surveyId);
    }

    /**
     * 임시저장 삭제 (최종 제출 후 호출)
     */
    public void deleteDraft(Long surveyId, Long empId) {
        surveyDraftRepository.delete(empId, surveyId);
        log.info("[deleteDraft] 임시저장 삭제: surveyId={}, empId={}", surveyId, empId);
    }
}
