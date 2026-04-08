package com.woongjin.survey.domain.employee.domain;

import com.woongjin.survey.domain.employee.domain.enums.EmployeeRole;
import com.woongjin.survey.domain.employee.domain.enums.Gender;
import com.woongjin.survey.domain.employee.domain.enums.Position;
import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 직원 엔티티 - EMPLOYEE 테이블 매핑
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emp_tb")
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_ID")
    private Long id;

    @Column(name = "ENO", nullable = false, length = 20)
    private String empNo;

    @Column(name = "EMP_NM", nullable = false, length = 10)
    private String empName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 10)
    private EmployeeRole role;

    @Column(name = "EMP_PW", nullable = false, length = 60) // 컬럼명 및 길이(60) 수정
    private String empPw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_ID", nullable = false) // Not Null 추가
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "CLPS", nullable = false, length = 10) // 컬럼명 수정
    private Position position;

    @Column(name = "MAIL", nullable = false, length = 20) // 컬럼명 및 길이(20) 수정
    private String email;

    /**
     * DDL에서 HGOF_YN이 tinyint(1)로 설정되어 있으므로 Boolean 타입으로 매핑 변경.
     * (기존 EmployeeStatus Enum을 유지하려면 AttributeConverter가 필요합니다.)
     */
    @Column(name = "HGOF_YN", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean empStatus;

    @Column(name = "BRTHY", nullable = false) // 컬럼명 수정
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "SXDN", nullable = false, length = 5) // 컬럼명 및 길이(5) 수정
    private Gender gender;
}