package com.example.reports.repository;

import com.example.reports.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Department entity.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
