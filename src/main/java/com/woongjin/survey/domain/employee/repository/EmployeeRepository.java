package com.woongjin.survey.domain.employee.repository;

import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmpNoAndEmpStatus(String empNo, Boolean status);

    Optional<Employee> findByIdAndEmpStatus(Long id, Boolean status);

    Optional<Employee> findByEmpNo(String empNo);
}
