package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ReportGenerationService for Employee-Department reports.
 * 
 * Orchestrates the entire report generation workflow:
 * 1. Load data via ReportDataProvider
 * 2. Export to file via ReportFileExporter
 * 3. Handle failures and archival
 * 
 * Design notes:
 * - Uses constructor injection for dependencies (DIP)
 * - Delegates to specialized components (SRP)
 * - Can be easily tested with mocks
 */
@Service
public class EmployeeDepartmentReportService implements ReportGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDepartmentReportService.class);

    private final ReportDataProvider dataProvider;
    private final ReportFileExporter fileExporter;

    public EmployeeDepartmentReportService(
            ReportDataProvider dataProvider,
            ReportFileExporter fileExporter) {
        this.dataProvider = dataProvider;
        this.fileExporter = fileExporter;
    }

    @Override
    public List<EmployeeDepartmentReportRow> loadReportData() {
        logger.info("Loading report data");
        return dataProvider.loadReportData();
    }

    @Override
    public Path generateAndStoreReport() throws Exception {
        logger.info("Starting report generation");
        
        try {
            // Load data
            List<EmployeeDepartmentReportRow> data = loadReportData();
            
            if (data.isEmpty()) {
                logger.warn("No data available for report generation");
            }
            
            // Export to file
            LocalDateTime reportTime = LocalDateTime.now();
            Path outputFile = fileExporter.exportToFile(data, reportTime);
            
            logger.info("Report generation completed successfully: {}", outputFile);
            return outputFile;
            
        } catch (Exception e) {
            logger.error("Report generation failed", e);
            throw new ReportGenerationException("Failed to generate report", e);
        }
    }

    @Override
    public int archiveOldReports() {
        logger.info("Archiving old reports");
        return fileExporter.archiveOldReports();
    }

    /**
     * Custom exception for report generation failures.
     * Provides clear domain-specific error context.
     */
    public static class ReportGenerationException extends Exception {
        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
