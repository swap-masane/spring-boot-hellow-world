package com.example.reports.web;

import com.example.reports.domain.EmployeeDepartmentReportRow;
import com.example.reports.file.CsvFileManager;
import com.example.reports.service.ReportGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests using Spring MVC Test framework.
 * 
 * Uses @WebMvcTest to:
 * - Auto-configure Spring MVC infrastructure
 * - Load only the web layer (not full application context)
 * - Mock service layer dependencies
 */
@WebMvcTest(EmployeeDepartmentReportController.class)
class EmployeeDepartmentReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportGenerationService reportService;

    @MockBean
    private CsvFileManager fileManager;

    @Test
    void testGetReportAsJson_Success() throws Exception {
        // Given
        List<EmployeeDepartmentReportRow> mockData = Arrays.asList(
                new EmployeeDepartmentReportRow("Alice Johnson", "Engineering", new BigDecimal("75000.00")),
                new EmployeeDepartmentReportRow("Bob Smith", "Sales", new BigDecimal("65000.00"))
        );
        when(reportService.loadReportData()).thenReturn(mockData);

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employeeName").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].departmentName").value("Engineering"))
                .andExpect(jsonPath("$[0].salary").value(75000.00))
                .andExpect(jsonPath("$[1].employeeName").value("Bob Smith"))
                .andExpect(jsonPath("$[1].departmentName").value("Sales"))
                .andExpect(jsonPath("$[1].salary").value(65000.00));

        verify(reportService, times(1)).loadReportData();
    }

    @Test
    void testGetReportAsJson_EmptyResult() throws Exception {
        // Given
        when(reportService.loadReportData()).thenReturn(Arrays.asList());

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetReportAsJson_ServiceError() throws Exception {
        // Given
        when(reportService.loadReportData()).thenThrow(new RuntimeException("Service error"));

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDownloadLatestCsv_ExistingFile() throws Exception {
        // Given
        Path mockFile = Files.createTempFile("report_", ".csv");
        Files.writeString(mockFile, "Employee Name,Department Name,Salary\nAlice,Engineering,75000");

        when(fileManager.getLatestOutputFile()).thenReturn(mockFile);

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department/csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Alice")));

        verify(fileManager, times(1)).getLatestOutputFile();
        verify(reportService, never()).generateAndStoreReport();

        // Cleanup
        Files.deleteIfExists(mockFile);
    }

    @Test
    void testDownloadLatestCsv_GenerateOnDemand() throws Exception {
        // Given
        Path mockFile = Files.createTempFile("report_", ".csv");
        Files.writeString(mockFile, "Employee Name,Department Name,Salary\nBob,Sales,65000");

        when(fileManager.getLatestOutputFile()).thenReturn(null); // No existing file
        when(reportService.generateAndStoreReport()).thenReturn(mockFile);

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department/csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bob")));

        verify(fileManager, times(1)).getLatestOutputFile();
        verify(reportService, times(1)).generateAndStoreReport();

        // Cleanup
        Files.deleteIfExists(mockFile);
    }

    @Test
    void testDownloadLatestCsv_NoFileAvailable() throws Exception {
        // Given
        when(fileManager.getLatestOutputFile()).thenReturn(null);
        when(reportService.generateAndStoreReport()).thenReturn(null);

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department/csv"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadLatestCsv_ServiceError() throws Exception {
        // Given
        when(fileManager.getLatestOutputFile()).thenReturn(null);
        when(reportService.generateAndStoreReport()).thenThrow(new RuntimeException("Generation failed"));

        // When/Then
        mockMvc.perform(get("/api/reports/employee-department/csv"))
                .andExpect(status().isInternalServerError());
    }
}
