package com.example.reports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;

/**
 * Configuration properties for report generation.
 * Externalized configuration following Spring Boot best practices.
 * 
 * Configurable via application.properties/yml:
 * - report.base-directory: Root directory for all report files
 * - report.time-zone: Time zone for scheduling (default: Europe/Berlin for CET)
 * - report.output-retention: Duration to keep files in output before archiving
 * - report.temp-folder, output-folder, error-folder, archive-folder: Subfolder names
 */
@Component
@ConfigurationProperties(prefix = "report")
@Validated
public class ReportProperties {

    @NotBlank
    private String baseDirectory = "/var/reports/employee-department";

    @NotBlank
    private String timeZone = "Europe/Berlin";

    @NotNull
    private Duration outputRetention = Duration.ofDays(7);

    private String tempFolder = "temp";
    private String outputFolder = "output";
    private String errorFolder = "error";
    private String archiveFolder = "archive";

    // Getters and Setters
    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Duration getOutputRetention() {
        return outputRetention;
    }

    public void setOutputRetention(Duration outputRetention) {
        this.outputRetention = outputRetention;
    }

    public String getTempFolder() {
        return tempFolder;
    }

    public void setTempFolder(String tempFolder) {
        this.tempFolder = tempFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String getErrorFolder() {
        return errorFolder;
    }

    public void setErrorFolder(String errorFolder) {
        this.errorFolder = errorFolder;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public void setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
    }
}
