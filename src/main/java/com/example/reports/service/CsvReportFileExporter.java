package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;
import com.example.reports.file.CsvFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CSV implementation of ReportFileExporter.
 * 
 * Delegates file operations to CsvFileManager while providing
 * the export workflow coordination.
 */
@Service
public class CsvReportFileExporter implements ReportFileExporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvReportFileExporter.class);

    private final CsvFileManager fileManager;

    public CsvReportFileExporter(CsvFileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public Path exportToFile(List<EmployeeDepartmentReportRow> data, LocalDateTime reportTime) throws Exception {
        logger.info("Starting CSV export for {} rows", data.size());
        
        // Create temporary file
        Path tempFile = fileManager.createTempCsvFile();
        
        try {
            // Write data to CSV
            fileManager.writeRowsToCsv(tempFile, data);
            
            // Move to output with final name
            Path outputFile = fileManager.moveToOutputWithFinalName(tempFile, reportTime);
            
            logger.info("CSV export completed successfully: {}", outputFile);
            return outputFile;
            
        } catch (Exception e) {
            logger.error("CSV export failed", e);
            // Move to error folder
            fileManager.moveToError(tempFile, e);
            throw e;
        }
    }

    @Override
    public void handleExportFailure(Path tempFile, Exception exception) {
        logger.error("Handling export failure", exception);
        if (tempFile != null) {
            fileManager.moveToError(tempFile, exception);
        }
    }

    @Override
    public int archiveOldReports() {
        logger.info("Archiving old reports");
        return fileManager.archiveOldFiles();
    }
}
