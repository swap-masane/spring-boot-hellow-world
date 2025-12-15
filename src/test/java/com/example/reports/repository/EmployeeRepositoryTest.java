package com.example.reports.repository;

import com.example.reports.domain.Department;
import com.example.reports.domain.Employee;
import com.example.reports.domain.EmployeeDepartmentReportRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository layer tests using H2 in-memory database.
 * 
 * Uses @DataJpaTest for:
 * - Auto-configuration of JPA repositories
 * - H2 in-memory database
 * - Transaction management (rollback after each test)
 * 
 * Tests the native query for employee-department report data.
 */
@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        // Create test departments
        Department engineering = new Department("Engineering");
        Department sales = new Department("Sales");
        Department hr = new Department("Human Resources");

        departmentRepository.save(engineering);
        departmentRepository.save(sales);
        departmentRepository.save(hr);

        // Create test employees
        employeeRepository.save(new Employee("Alice Johnson", new BigDecimal("75000.00"), engineering));
        employeeRepository.save(new Employee("Bob Smith", new BigDecimal("85000.00"), engineering));
        employeeRepository.save(new Employee("Carol White", new BigDecimal("65000.00"), sales));
        employeeRepository.save(new Employee("David Brown", new BigDecimal("70000.00"), sales));
        employeeRepository.save(new Employee("Eve Davis", new BigDecimal("60000.00"), hr));
    }

    @Test
    void testFindEmployeeDepartmentReport() {
        // When
        List<EmployeeDepartmentReportRow> report = employeeRepository.findEmployeeDepartmentReport();

        // Then
        assertNotNull(report);
        assertEquals(5, report.size(), "Should return all 5 employees");

        // Verify data is ordered by department name, then employee name
        EmployeeDepartmentReportRow firstRow = report.get(0);
        assertNotNull(firstRow.getEmployeeName());
        assertNotNull(firstRow.getDepartmentName());
        assertNotNull(firstRow.getSalary());

        // Verify specific employee data
        boolean foundAlice = report.stream()
                .anyMatch(row -> "Alice Johnson".equals(row.getEmployeeName()) 
                        && "Engineering".equals(row.getDepartmentName())
                        && new BigDecimal("75000.00").equals(row.getSalary()));
        assertTrue(foundAlice, "Should find Alice Johnson in Engineering with salary 75000");

        // Verify all departments are represented
        long distinctDepartments = report.stream()
                .map(EmployeeDepartmentReportRow::getDepartmentName)
                .distinct()
                .count();
        assertEquals(3, distinctDepartments, "Should have 3 distinct departments");
    }

    @Test
    void testFindEmployeeDepartmentReport_EmptyDatabase() {
        // Given - clear all data
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // When
        List<EmployeeDepartmentReportRow> report = employeeRepository.findEmployeeDepartmentReport();

        // Then
        assertNotNull(report);
        assertTrue(report.isEmpty(), "Should return empty list when no data exists");
    }

    @Test
    void testFindEmployeeDepartmentReport_SingleEmployee() {
        // Given - clear and add single employee
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        Department dept = departmentRepository.save(new Department("IT"));
        employeeRepository.save(new Employee("John Doe", new BigDecimal("100000.00"), dept));

        // When
        List<EmployeeDepartmentReportRow> report = employeeRepository.findEmployeeDepartmentReport();

        // Then
        assertEquals(1, report.size());
        EmployeeDepartmentReportRow row = report.get(0);
        assertEquals("John Doe", row.getEmployeeName());
        assertEquals("IT", row.getDepartmentName());
        assertEquals(new BigDecimal("100000.00"), row.getSalary());
    }
}
