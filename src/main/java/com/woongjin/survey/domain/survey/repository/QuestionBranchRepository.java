package com.woongjin.survey.domain.survey.repository;

import com.woongjin.survey.domain.survey.domain.QuestionBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuestionBranchRepository extends JpaRepository<QuestionBranch, Long> {

    /**
     * 주어진 부모 문항 ID 목록에 속한 분기 정보를 조회.
     * 호출자가 설문의 문항 ID 목록을 이미 가지고 있다는 가정 하에 사용.
     */
    List<QuestionBranch> findByParentQuestionIdIn(Collection<Long> parentQuestionIds);
}
