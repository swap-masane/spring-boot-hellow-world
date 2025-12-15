package com.example.reports.repository;

import com.example.reports.domain.Employee;
import com.example.reports.domain.EmployeeDepartmentReportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Employee entity.
 * Contains a native query to join EMPLOYEE and DEPARTMENT tables
 * and return report data as EmployeeDepartmentReportRow projections.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Native SQL query joining EMPLOYEE and DEPARTMENT tables.
     * Returns a projection with employee name, department name, and salary.
     * 
     * This query demonstrates:
     * - Native SQL with Spring Data JPA
     * - Constructor-based projection for EmployeeDepartmentReportRow
     * - JOIN operation across multiple tables
     */
    @Query(value = "SELECT new com.example.reports.domain.EmployeeDepartmentReportRow(" +
                   "e.name, d.name, e.salary) " +
                   "FROM Employee e JOIN e.department d " +
                   "ORDER BY d.name, e.name",
           nativeQuery = false)
    List<EmployeeDepartmentReportRow> findEmployeeDepartmentReport();
    
    /**
     * Alternative native SQL query (for SQL Server).
     * This can be used when pure native SQL is required.
     */
    @Query(value = "SELECT e.name AS employeeName, d.name AS departmentName, e.salary " +
                   "FROM EMPLOYEE e " +
                   "INNER JOIN DEPARTMENT d ON e.department_id = d.id " +
                   "ORDER BY d.name, e.name",
           nativeQuery = true)
    List<Object[]> findEmployeeDepartmentReportNative();
}
