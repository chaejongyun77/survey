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
@Table(name = "department")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long id;

    @NotNull
    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;

    /**
     * 상위 부서 (Self-referencing FK)
     * - 최상위 부서는 null
     * - LAZY: 부서 조회 시 상위 부서를 매번 JOIN하지 않도록
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dept_id")
    private Department parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Department> children = new ArrayList<>();

    @NotNull
    @Column(name = "depth")
    private Integer depth;
}
