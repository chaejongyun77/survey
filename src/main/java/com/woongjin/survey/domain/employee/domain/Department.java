package com.woongjin.survey.domain.employee.domain;

import com.woongjin.survey.global.jpa.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 부서 엔티티 - DEPARTMENT 테이블 매핑
 *
 * 인접 리스트(Adjacency List) 방식으로 계층 구조를 표현
 * - parent: 상위 부서 (null 이면 최상위 부서)
 * - children: 하위 부서 목록
 * - depth: 0 = 최상위, 1 = 1단계 하위, ...
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "dept_tb")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPT_ID") // 컬럼명 수정
    private Long id;

    @Column(name = "DEPT_NM", nullable = false, length = 20) // 컬럼명 및 길이 수정 (varchar(20))
    private String deptName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UP_DEPT_ID", nullable = false) // 컬럼명 수정
    private Department parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Department> children = new ArrayList<>();

    @Column(name = "DEPT_LVL", nullable = false) // 컬럼명 수정
    private Integer depth;
}
