package com.example.reports.file;

import com.example.reports.config.ReportProperties;
import com.example.reports.domain.EmployeeDepartmentReportRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * File manager tests using JUnit 5 @TempDir.
 * 
 * Tests file operations in isolation using a temporary directory
 * that is automatically cleaned up after tests.
 */
class CsvFileManagerTest {

    @TempDir
    Path tempDirectory;

    private CsvFileManager fileManager;
    private ReportProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ReportProperties();
        properties.setBaseDirectory(tempDirectory.toString());
        properties.setOutputRetention(Duration.ofSeconds(1)); // Short retention for testing
        fileManager = new CsvFileManager(properties);
    }

    @Test
    void testCreateTempCsvFile() throws IOException {
        // When
        Path tempFile = fileManager.createTempCsvFile();

        // Then
        assertNotNull(tempFile);
        assertTrue(Files.exists(tempFile), "Temp file should exist");
        assertTrue(tempFile.getFileName().toString().startsWith("report_temp_"));
        assertTrue(tempFile.getFileName().toString().endsWith(".csv"));
    }

    @Test
    void testWriteRowsToCsv() throws IOException {
        // Given
        Path tempFile = fileManager.createTempCsvFile();
        List<EmployeeDepartmentReportRow> rows = Arrays.asList(
                new EmployeeDepartmentReportRow("Alice Johnson", "Engineering", new BigDecimal("75000.00")),
                new EmployeeDepartmentReportRow("Bob Smith", "Sales", new BigDecimal("65000.00"))
        );

        // When
        fileManager.writeRowsToCsv(tempFile, rows);

        // Then
        assertTrue(Files.exists(tempFile));
        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(3, lines.size(), "Should have header + 2 data rows");
        assertEquals("Employee Name,Department Name,Salary", lines.get(0));
        assertTrue(lines.get(1).contains("Alice Johnson"));
        assertTrue(lines.get(1).contains("Engineering"));
        assertTrue(lines.get(1).contains("75000.00"));
    }

    @Test
    void testWriteRowsToCsv_EmptyList() throws IOException {
        // Given
        Path tempFile = fileManager.createTempCsvFile();
        List<EmployeeDepartmentReportRow> emptyRows = Arrays.asList();

        // When
        fileManager.writeRowsToCsv(tempFile, emptyRows);

        // Then
        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(1, lines.size(), "Should only have header row");
        assertEquals("Employee Name,Department Name,Salary", lines.get(0));
    }

    @Test
    void testMoveToOutputWithFinalName() throws IOException {
        // Given
        Path tempFile = fileManager.createTempCsvFile();
        Files.writeString(tempFile, "test content");
        LocalDateTime reportTime = LocalDateTime.of(2023, 12, 15, 10, 30, 0);

        // When
        Path outputFile = fileManager.moveToOutputWithFinalName(tempFile, reportTime);

        // Then
        assertNotNull(outputFile);
        assertTrue(Files.exists(outputFile), "Output file should exist");
        assertFalse(Files.exists(tempFile), "Temp file should be moved");
        assertEquals("report_15122023_103000.csv", outputFile.getFileName().toString());
        assertEquals("test content", Files.readString(outputFile));
    }

    @Test
    void testMoveToError() throws IOException {
        // Given
        Path tempFile = fileManager.createTempCsvFile();
        Files.writeString(tempFile, "error content");
        Exception testException = new RuntimeException("Test error");

        // When
        Path errorFile = fileManager.moveToError(tempFile, testException);

        // Then
        assertNotNull(errorFile);
        assertTrue(Files.exists(errorFile), "Error file should exist");
        assertFalse(Files.exists(tempFile), "Temp file should be moved");
        assertTrue(errorFile.getFileName().toString().startsWith("error_"));
        assertTrue(errorFile.getFileName().toString().endsWith(".csv"));
    }

    @Test
    void testMoveToError_NonExistentFile() {
        // Given
        Path nonExistentFile = tempDirectory.resolve("nonexistent.csv");
        Exception testException = new RuntimeException("Test error");

        // When
        Path errorFile = fileManager.moveToError(nonExistentFile, testException);

        // Then
        assertNull(errorFile, "Should return null when file doesn't exist");
    }

    @Test
    void testArchiveOldFiles() throws IOException, InterruptedException {
        // Given - create some output files
        Path outputDir = tempDirectory.resolve("output");
        Files.createDirectories(outputDir);

        Path oldFile1 = outputDir.resolve("report_old1.csv");
        Path oldFile2 = outputDir.resolve("report_old2.csv");
        Files.writeString(oldFile1, "old content 1");
        Files.writeString(oldFile2, "old content 2");

        // Wait for retention period to pass
        Thread.sleep(1500); // Retention is 1 second

        Path newFile = outputDir.resolve("report_new.csv");
        Files.writeString(newFile, "new content");

        // When
        int archivedCount = fileManager.archiveOldFiles();

        // Then
        assertEquals(2, archivedCount, "Should archive 2 old files");
        assertFalse(Files.exists(oldFile1), "Old file 1 should be archived");
        assertFalse(Files.exists(oldFile2), "Old file 2 should be archived");
        assertTrue(Files.exists(newFile), "New file should not be archived");

        // Verify files are in archive
        Path archiveDir = tempDirectory.resolve("archive");
        assertTrue(Files.exists(archiveDir.resolve("report_old1.csv")));
        assertTrue(Files.exists(archiveDir.resolve("report_old2.csv")));
    }

    @Test
    void testGetLatestOutputFile() throws IOException {
        // Given
        Path outputDir = tempDirectory.resolve("output");
        Files.createDirectories(outputDir);

        Path file1 = outputDir.resolve("report_1.csv");
        Path file2 = outputDir.resolve("report_2.csv");
        Files.writeString(file1, "content 1");
        Thread.sleep(10); // Small delay to ensure different timestamps
        Files.writeString(file2, "content 2");

        // When
        Path latest = fileManager.getLatestOutputFile();

        // Then
        assertNotNull(latest);
        assertEquals("report_2.csv", latest.getFileName().toString());
    }

    @Test
    void testGetLatestOutputFile_NoFiles() {
        // When
        Path latest = fileManager.getLatestOutputFile();

        // Then
        assertNull(latest, "Should return null when no files exist");
    }
}
