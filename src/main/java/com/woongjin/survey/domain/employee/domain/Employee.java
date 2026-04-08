package com.woongjin.survey.domain.employee.domain;

import com.woongjin.survey.domain.employee.domain.enums.EmployeeRole;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeStatus;
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
@Table(name = "employee")
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Long id;

    @NotNull
    @Column(name = "emp_no", length = 20)
    private String empNo;

    @NotNull
    @Column(name = "emp_name", length = 10)
    private String empName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EmployeeRole role;

    @NotNull
    @Column(nullable = false, length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Position position;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private EmployeeStatus status;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 6)
    private Gender gender;
}
