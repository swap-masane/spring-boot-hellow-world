package com.example.reports.domain;

import java.math.BigDecimal;

/**
 * DTO/Projection for Employee-Department report data.
 * Represents a single row in the report with employee name, department name, and salary.
 */
public class EmployeeDepartmentReportRow {

    private String employeeName;
    private String departmentName;
    private BigDecimal salary;

    // Constructor for JPA native query projection
    public EmployeeDepartmentReportRow(String employeeName, String departmentName, BigDecimal salary) {
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.salary = salary;
    }

    // Default constructor
    public EmployeeDepartmentReportRow() {
    }

    // Getters and Setters
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "EmployeeDepartmentReportRow{" +
                "employeeName='" + employeeName + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", salary=" + salary +
                '}';
    }
}
