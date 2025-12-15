package com.example.reports.job;

import com.example.reports.service.ReportGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Scheduled job for generating Employee-Department reports.
 * 
 * Runs daily at 7:00 AM CET (Europe/Berlin timezone).
 * 
 * Workflow:
 * 1. Generate and store report
 * 2. Archive old reports (retention policy)
 * 3. Handle failures gracefully
 * 
 * Design notes:
 * - Uses @Scheduled with cron expression for precise timing
 * - Delegates business logic to ReportGenerationService
 * - Comprehensive error handling and logging
 * - Ensures archival runs even after successful generation
 * 
 * Extension point: Multiple report jobs can be created by implementing
 * similar classes with different schedules and service dependencies.
 */
@Component
public class EmployeeDepartmentReportJob {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDepartmentReportJob.class);

    private final ReportGenerationService reportService;

    public EmployeeDepartmentReportJob(ReportGenerationService reportService) {
        this.reportService = reportService;
    }

    /**
     * Scheduled method that runs daily at 7:00 AM CET.
     * 
     * Cron expression: "0 0 7 * * *" means:
     * - 0 seconds
     * - 0 minutes
     * - 7 hours (7 AM)
     * - Every day of month
     * - Every month
     * - Every day of week
     * 
     * Zone: "Europe/Berlin" for CET/CEST timezone
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Europe/Berlin")
    public void runDailyReport() {
        logger.info("=== Starting scheduled Employee-Department report generation ===");
        
        try {
            // Generate and store the report
            Path reportFile = reportService.generateAndStoreReport();
            logger.info("Report successfully generated: {}", reportFile);
            
            // Archive old reports after successful generation
            int archivedCount = reportService.archiveOldReports();
            logger.info("Archived {} old report files", archivedCount);
            
            logger.info("=== Scheduled report generation completed successfully ===");
            
        } catch (Exception e) {
            // Log the error with full context
            logger.error("=== Scheduled report generation FAILED ===", e);
            logger.error("Error details: {}", e.getMessage());
            
            // Note: The service layer has already moved temp files to error folder
            // Additional notifications (email, alerts) could be added here
        }
    }

    /**
     * Optional: On-demand report generation (can be triggered via admin API).
     * Not scheduled, can be called programmatically.
     */
    public void runReportOnDemand() {
        logger.info("Running on-demand report generation");
        runDailyReport();
    }
}
