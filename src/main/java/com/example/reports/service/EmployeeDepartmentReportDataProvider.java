package com.example.reports.service;

import com.example.reports.domain.EmployeeDepartmentReportRow;
import com.example.reports.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ReportDataProvider using Employee repository.
 * 
 * Responsibilities:
 * - Execute repository query to fetch employee-department data
 * - Handle data access exceptions
 * - Provide transactional context for query execution
 */
@Service
public class EmployeeDepartmentReportDataProvider implements ReportDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDepartmentReportDataProvider.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeDepartmentReportDataProvider(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDepartmentReportRow> loadReportData() {
        logger.info("Loading employee-department report data");
        List<EmployeeDepartmentReportRow> data = employeeRepository.findEmployeeDepartmentReport();
        logger.info("Loaded {} report rows", data.size());
        return data;
    }
}
