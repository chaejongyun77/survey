package com.woongjin.survey.domain.employee.repository;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmpNoAndStatus(String empNo, EmployeeStatus status);

    Optional<Employee> findByIdAndStatus(Long id, EmployeeStatus status);
}
