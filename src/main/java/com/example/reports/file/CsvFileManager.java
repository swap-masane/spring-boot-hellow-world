package com.example.reports.file;

import com.example.reports.config.ReportProperties;
import com.example.reports.domain.EmployeeDepartmentReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manages CSV file operations for report generation.
 * 
 * Responsibilities:
 * - Creating temporary CSV files
 * - Writing report data to CSV
 * - Moving files to output/error/archive folders
 * - Archiving old files based on retention policy
 * 
 * Design notes:
 * - Uses Java NIO (Files API) for robust file operations
 * - ATOMIC_MOVE for transactional file operations where supported
 * - Proper resource management with try-with-resources
 * - Comprehensive logging for troubleshooting
 * 
 * Extension point: Can be extended to support different file formats
 * by implementing a FileManager interface.
 */
@Component
public class CsvFileManager {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileManager.class);
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");

    private final ReportProperties properties;
    private final Path baseDir;
    private final Path tempDir;
    private final Path outputDir;
    private final Path errorDir;
    private final Path archiveDir;

    public CsvFileManager(ReportProperties properties) {
        this.properties = properties;
        this.baseDir = Paths.get(properties.getBaseDirectory());
        this.tempDir = baseDir.resolve(properties.getTempFolder());
        this.outputDir = baseDir.resolve(properties.getOutputFolder());
        this.errorDir = baseDir.resolve(properties.getErrorFolder());
        this.archiveDir = baseDir.resolve(properties.getArchiveFolder());
        
        initializeDirectories();
    }

    /**
     * Creates required directories if they don't exist.
     */
    private void initializeDirectories() {
        try {
            Files.createDirectories(tempDir);
            Files.createDirectories(outputDir);
            Files.createDirectories(errorDir);
            Files.createDirectories(archiveDir);
            logger.info("Initialized report directories under: {}", baseDir);
        } catch (IOException e) {
            logger.error("Failed to initialize report directories", e);
            throw new IllegalStateException("Cannot initialize report directories", e);
        }
    }

    /**
     * Creates a temporary CSV file with a unique name.
     * 
     * @return Path to the created temporary file
     * @throws IOException if file creation fails
     */
    public Path createTempCsvFile() throws IOException {
        String tempFileName = "report_temp_" + System.currentTimeMillis() + ".csv";
        Path tempFile = tempDir.resolve(tempFileName);
        Files.createFile(tempFile);
        logger.debug("Created temporary file: {}", tempFile);
        return tempFile;
    }

    /**
     * Writes report data rows to a CSV file.
     * Format: Header row + data rows with comma-separated values.
     * 
     * @param tempFile Path to temporary CSV file
     * @param rows List of report rows to write
     * @throws IOException if writing fails
     */
    public void writeRowsToCsv(Path tempFile, List<EmployeeDepartmentReportRow> rows) throws IOException {
        logger.info("Writing {} rows to CSV file: {}", rows.size(), tempFile);
        
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardOpenOption.WRITE)) {
            // Write CSV header
            writer.write("Employee Name,Department Name,Salary");
            writer.newLine();
            
            // Write data rows
            for (EmployeeDepartmentReportRow row : rows) {
                writer.write(String.format("%s,%s,%s",
                    escapeCsvValue(row.getEmployeeName()),
                    escapeCsvValue(row.getDepartmentName()),
                    row.getSalary()));
                writer.newLine();
            }
        }
        
        logger.info("Successfully wrote CSV file: {}", tempFile);
    }

    /**
     * Escapes CSV values to handle special characters (commas, quotes, newlines).
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Moves temporary file to output directory with final name.
     * Final name format: report_ddMMyyyy_HHmmss.csv
     * 
     * @param tempFile Path to temporary file
     * @param reportTime Timestamp for the report
     * @return Path to the final output file
     * @throws IOException if move operation fails
     */
    public Path moveToOutputWithFinalName(Path tempFile, LocalDateTime reportTime) throws IOException {
        String finalFileName = "report_" + reportTime.format(FILE_NAME_FORMATTER) + ".csv";
        Path outputFile = outputDir.resolve(finalFileName);
        
        // Use ATOMIC_MOVE where supported, fallback to REPLACE_EXISTING
        try {
            Files.move(tempFile, outputFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            logger.warn("Atomic move not supported, using standard move");
            Files.move(tempFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        logger.info("Moved file to output: {}", outputFile);
        return outputFile;
    }

    /**
     * Moves temporary file to error directory when report generation fails.
     * Includes exception details in the filename for troubleshooting.
     * 
     * @param tempFile Path to temporary file
     * @param e Exception that caused the failure
     * @return Path to the error file
     */
    public Path moveToError(Path tempFile, Exception e) {
        try {
            if (!Files.exists(tempFile)) {
                logger.warn("Temporary file does not exist, cannot move to error: {}", tempFile);
                return null;
            }
            
            String errorFileName = "error_" + System.currentTimeMillis() + ".csv";
            Path errorFile = errorDir.resolve(errorFileName);
            Files.move(tempFile, errorFile, StandardCopyOption.REPLACE_EXISTING);
            
            logger.error("Moved failed file to error directory: {} - Reason: {}", 
                errorFile, e.getMessage());
            return errorFile;
        } catch (IOException ioEx) {
            logger.error("Failed to move file to error directory", ioEx);
            return null;
        }
    }

    /**
     * Archives old files from output directory based on retention policy.
     * Files older than the configured retention period are moved to archive.
     * 
     * @return Number of files archived
     */
    public int archiveOldFiles() {
        logger.info("Starting archival process for files older than {}", properties.getOutputRetention());
        int archivedCount = 0;
        
        try (Stream<Path> files = Files.list(outputDir)) {
            LocalDateTime cutoffTime = LocalDateTime.now().minus(properties.getOutputRetention());
            
            List<Path> oldFiles = files
                .filter(Files::isRegularFile)
                .filter(file -> {
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file).toInstant(),
                            java.time.ZoneId.systemDefault()
                        );
                        return fileTime.isBefore(cutoffTime);
                    } catch (IOException e) {
                        logger.warn("Could not get modified time for file: {}", file, e);
                        return false;
                    }
                })
                .toList();
            
            for (Path oldFile : oldFiles) {
                try {
                    Path archiveFile = archiveDir.resolve(oldFile.getFileName());
                    Files.move(oldFile, archiveFile, StandardCopyOption.REPLACE_EXISTING);
                    archivedCount++;
                    logger.info("Archived file: {} -> {}", oldFile.getFileName(), archiveFile);
                } catch (IOException e) {
                    logger.error("Failed to archive file: {}", oldFile, e);
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to list files in output directory", e);
        }
        
        logger.info("Archival complete. Archived {} files", archivedCount);
        return archivedCount;
    }

    /**
     * Gets the latest CSV file from the output directory.
     * 
     * @return Path to the most recent output file, or null if none exists
     */
    public Path getLatestOutputFile() {
        try (Stream<Path> files = Files.list(outputDir)) {
            return files
                .filter(Files::isRegularFile)
                .filter(file -> file.getFileName().toString().endsWith(".csv"))
                .max((f1, f2) -> {
                    try {
                        return Files.getLastModifiedTime(f1).compareTo(Files.getLastModifiedTime(f2));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .orElse(null);
        } catch (IOException e) {
            logger.error("Failed to get latest output file", e);
            return null;
        }
    }
}
