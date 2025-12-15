package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for exporting report data to files.
 * 
 * Following Interface Segregation Principle (ISP):
 * - Focused on file export operations only
 * - Separate from data loading concerns
 * 
 * Extension point: Can have implementations for different formats
 * (CSV, Excel, PDF, etc.)
 */
public interface ReportFileExporter {

    /**
     * Exports report data to a file and returns the final output path.
     * 
     * @param data Report data to export
     * @param reportTime Timestamp for the report (used in filename)
     * @return Path to the final output file
     * @throws Exception if export fails at any stage
     */
    Path exportToFile(List<EmployeeDepartmentReportRow> data, LocalDateTime reportTime) throws Exception;

    /**
     * Handles failed export by moving temporary file to error directory.
     * 
     * @param tempFile Path to temporary file (may be null)
     * @param exception Exception that caused the failure
     */
    void handleExportFailure(Path tempFile, Exception exception);

    /**
     * Archives old report files based on retention policy.
     * 
     * @return Number of files archived
     */
    int archiveOldReports();
}
