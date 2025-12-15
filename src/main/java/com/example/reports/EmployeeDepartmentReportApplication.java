package com.example.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class.
 * 
 * Annotations:
 * - @SpringBootApplication: Enables auto-configuration, component scanning, and configuration
 * - @EnableScheduling: Enables Spring's scheduled task execution capability
 * 
 * This application provides:
 * - Scheduled daily reports at 7:00 AM CET
 * - REST API for report data (JSON and CSV)
 * - Automated file archival based on retention policy
 * - Robust error handling and logging
 * 
 * Configuration:
 * - Uses Java-based configuration (no XML)
 * - Externalized properties via application.properties/yml
 * - Profile-based configuration (dev, prod, test)
 */
@SpringBootApplication
@EnableScheduling
public class EmployeeDepartmentReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeDepartmentReportApplication.class, args);
    }
}
