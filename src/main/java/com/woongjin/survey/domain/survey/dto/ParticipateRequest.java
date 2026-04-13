package com.woongjin.survey.domain.survey.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설문 참여하기 요청 DTO
 * - 추후 JWT 인증 도입 시 empId는 토큰에서 추출하는 방식으로 교체 예정
 */
@Getter
@NoArgsConstructor
public class ParticipateRequest {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long empId;
}
