package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;

import java.util.List;

/**
 * Interface for loading report data from the data source.
 * 
 * Following Dependency Inversion Principle (DIP):
 * - High-level modules depend on this abstraction
 * - Low-level implementations (repository access) implement this interface
 * 
 * Extension point: Can have multiple implementations for different data sources
 * (e.g., database, REST API, file system)
 */
public interface ReportDataProvider {

    /**
     * Loads report data containing employee and department information.
     * 
     * @return List of report rows with employee name, department name, and salary
     */
    List<EmployeeDepartmentReportRow> loadReportData();
}
