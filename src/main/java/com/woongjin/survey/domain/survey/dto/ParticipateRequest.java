package com.woongjin.survey.domain.survey.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @deprecated empId 는 ClientInterceptor 가 주입하는 request attribute(empNo) 로 대체됨
 */
@Deprecated
@Getter
@NoArgsConstructor
public class ParticipateRequest {

    @NotNull(message = "사원 ID는 필수입니다.")
    private Long empId;
}
