package com.example.reports.web;

import com.example.reports.domain.EmployeeDepartmentReportRow;
import com.example.reports.file.CsvFileManager;
import com.example.reports.service.ReportGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

/**
 * REST controller for Employee-Department reports.
 * 
 * Endpoints:
 * - GET /api/reports/employee-department: Returns report data as JSON
 * - GET /api/reports/employee-department/csv: Downloads latest CSV report
 * 
 * Design notes:
 * - Thin controller, delegates to service layer
 * - Uses same service for both JSON and CSV endpoints (no duplication)
 * - Proper HTTP status codes and content types
 * - Exception handling with meaningful error responses
 * 
 * Extension point: Can add more endpoints for:
 * - Filtering by date range
 * - Downloading specific historical reports
 * - Triggering on-demand report generation
 */
@RestController
@RequestMapping("/api/reports/employee-department")
public class EmployeeDepartmentReportController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDepartmentReportController.class);

    private final ReportGenerationService reportService;
    private final CsvFileManager fileManager;

    public EmployeeDepartmentReportController(
            ReportGenerationService reportService,
            CsvFileManager fileManager) {
        this.reportService = reportService;
        this.fileManager = fileManager;
    }

    /**
     * Returns the employee-department report data as JSON.
     * 
     * @return List of report rows with employee name, department name, and salary
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeDepartmentReportRow>> getReportAsJson() {
        logger.info("GET /api/reports/employee-department - JSON format requested");
        
        try {
            List<EmployeeDepartmentReportRow> data = reportService.loadReportData();
            logger.info("Returning {} report rows as JSON", data.size());
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            logger.error("Failed to load report data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Downloads the latest generated CSV report file.
     * If no report exists, generates one on-demand.
     * 
     * @return CSV file as downloadable resource
     */
    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<Resource> downloadLatestCsv() {
        logger.info("GET /api/reports/employee-department/csv - CSV download requested");
        
        try {
            // Try to get the latest existing report
            Path latestFile = fileManager.getLatestOutputFile();
            
            // If no report exists, generate one on-demand
            if (latestFile == null) {
                logger.info("No existing report found, generating on-demand");
                latestFile = reportService.generateAndStoreReport();
            }
            
            if (latestFile == null) {
                logger.warn("No report file available");
                return ResponseEntity.notFound().build();
            }
            
            // Prepare file for download
            Resource resource = new FileSystemResource(latestFile);
            String filename = latestFile.getFileName().toString();
            
            logger.info("Serving CSV file: {}", filename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Failed to serve CSV report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
