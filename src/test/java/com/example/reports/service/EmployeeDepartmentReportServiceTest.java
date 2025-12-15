package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service layer tests using Mockito.
 * 
 * Tests the orchestration logic in EmployeeDepartmentReportService
 * without depending on actual repository or file system.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeDepartmentReportServiceTest {

    @Mock
    private ReportDataProvider dataProvider;

    @Mock
    private ReportFileExporter fileExporter;

    private EmployeeDepartmentReportService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeDepartmentReportService(dataProvider, fileExporter);
    }

    @Test
    void testLoadReportData() {
        // Given
        List<EmployeeDepartmentReportRow> mockData = Arrays.asList(
                new EmployeeDepartmentReportRow("Alice", "Engineering", new BigDecimal("75000")),
                new EmployeeDepartmentReportRow("Bob", "Sales", new BigDecimal("65000"))
        );
        when(dataProvider.loadReportData()).thenReturn(mockData);

        // When
        List<EmployeeDepartmentReportRow> result = service.loadReportData();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dataProvider, times(1)).loadReportData();
    }

    @Test
    void testGenerateAndStoreReport_Success() throws Exception {
        // Given
        List<EmployeeDepartmentReportRow> mockData = Arrays.asList(
                new EmployeeDepartmentReportRow("Alice", "Engineering", new BigDecimal("75000"))
        );
        Path expectedPath = Paths.get("/output/report_15122023_120000.csv");

        when(dataProvider.loadReportData()).thenReturn(mockData);
        when(fileExporter.exportToFile(any(), any())).thenReturn(expectedPath);

        // When
        Path result = service.generateAndStoreReport();

        // Then
        assertNotNull(result);
        assertEquals(expectedPath, result);
        verify(dataProvider, times(1)).loadReportData();
        verify(fileExporter, times(1)).exportToFile(eq(mockData), any(LocalDateTime.class));
    }

    @Test
    void testGenerateAndStoreReport_EmptyData() throws Exception {
        // Given
        List<EmployeeDepartmentReportRow> emptyData = Arrays.asList();
        Path expectedPath = Paths.get("/output/report_15122023_120000.csv");

        when(dataProvider.loadReportData()).thenReturn(emptyData);
        when(fileExporter.exportToFile(any(), any())).thenReturn(expectedPath);

        // When
        Path result = service.generateAndStoreReport();

        // Then
        assertNotNull(result);
        verify(dataProvider, times(1)).loadReportData();
        verify(fileExporter, times(1)).exportToFile(eq(emptyData), any(LocalDateTime.class));
    }

    @Test
    void testGenerateAndStoreReport_DataProviderFailure() {
        // Given
        when(dataProvider.loadReportData()).thenThrow(new RuntimeException("Database error"));

        // When/Then
        assertThrows(
                EmployeeDepartmentReportService.ReportGenerationException.class,
                () -> service.generateAndStoreReport()
        );

        verify(dataProvider, times(1)).loadReportData();
        verify(fileExporter, never()).exportToFile(any(), any());
    }

    @Test
    void testGenerateAndStoreReport_FileExporterFailure() throws Exception {
        // Given
        List<EmployeeDepartmentReportRow> mockData = Arrays.asList(
                new EmployeeDepartmentReportRow("Alice", "Engineering", new BigDecimal("75000"))
        );

        when(dataProvider.loadReportData()).thenReturn(mockData);
        when(fileExporter.exportToFile(any(), any())).thenThrow(new RuntimeException("IO error"));

        // When/Then
        assertThrows(
                EmployeeDepartmentReportService.ReportGenerationException.class,
                () -> service.generateAndStoreReport()
        );

        verify(dataProvider, times(1)).loadReportData();
        verify(fileExporter, times(1)).exportToFile(any(), any());
    }

    @Test
    void testArchiveOldReports() {
        // Given
        when(fileExporter.archiveOldReports()).thenReturn(3);

        // When
        int result = service.archiveOldReports();

        // Then
        assertEquals(3, result);
        verify(fileExporter, times(1)).archiveOldReports();
    }
}
