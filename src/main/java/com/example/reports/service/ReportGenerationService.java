package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;

import java.nio.file.Path;
import java.util.List;

/**
 * Orchestrates the complete report generation workflow.
 * 
 * Following Single Responsibility Principle (SRP):
 * - Coordinates between data loading and file export
 * - Does not handle low-level details of either concern
 * 
 * Extension point: Can be extended to generate different types of reports
 * by injecting different ReportDataProvider and ReportFileExporter implementations.
 */
public interface ReportGenerationService {

    /**
     * Loads report data from the configured data source.
     * 
     * @return List of report rows
     */
    List<EmployeeDepartmentReportRow> loadReportData();

    /**
     * Generates a complete report and stores it as a file.
     * 
     * Workflow:
     * 1. Load data from data provider
     * 2. Export data using file exporter
     * 3. Handle any failures appropriately
     * 
     * @return Path to the generated report file
     * @throws Exception if report generation fails
     */
    Path generateAndStoreReport() throws Exception;

    /**
     * Archives old report files based on retention policy.
     * Should be called after successful report generation.
     * 
     * @return Number of files archived
     */
    int archiveOldReports();
}
