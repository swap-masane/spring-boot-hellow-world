# Design and Architecture Documentation

## Overview

This document explains the key design decisions and architectural patterns used in the Employee-Department Report application.

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)

Each class has one clear responsibility:

- **EmployeeRepository**: Data access for Employee entities
- **CsvFileManager**: File operations (create, write, move, archive)
- **EmployeeDepartmentReportDataProvider**: Load report data from database
- **CsvReportFileExporter**: Export data to CSV format
- **EmployeeDepartmentReportService**: Orchestrate report generation workflow
- **EmployeeDepartmentReportJob**: Schedule and trigger report generation
- **EmployeeDepartmentReportController**: Handle HTTP requests/responses

### Open/Closed Principle (OCP)

The system is open for extension, closed for modification:

- **Interface-based design**: `ReportDataProvider`, `ReportFileExporter`, `ReportGenerationService` are interfaces
- **Multiple implementations**: Can add new report types by implementing these interfaces
- **Example extension**: Add `ExcelReportFileExporter` without modifying existing code
- **Multiple report generators**: The scheduler can work with multiple `ReportGenerator` beans

### Liskov Substitution Principle (LSP)

All implementations can substitute their interfaces:

- Any `ReportDataProvider` implementation can replace another
- Any `ReportFileExporter` implementation provides the same contract
- Tests use mocks interchangeably with real implementations

### Interface Segregation Principle (ISP)

Focused, minimal interfaces:

- `ReportDataProvider`: Only data loading concern
- `ReportFileExporter`: Only file export concern
- No "god interfaces" forcing implementations to provide unused methods

### Dependency Inversion Principle (DIP)

High-level modules depend on abstractions:

- `EmployeeDepartmentReportService` depends on `ReportDataProvider` and `ReportFileExporter` interfaces
- Concrete implementations are injected via Spring's dependency injection
- Easy to mock for testing
- Easy to swap implementations

## Architectural Layers

### Domain Layer
- **Entities**: `Employee`, `Department` (JPA entities)
- **DTOs**: `EmployeeDepartmentReportRow` (data transfer object)
- Pure domain objects with no framework dependencies

### Repository Layer
- Spring Data JPA repositories
- JPQL query for report data (portable across databases)
- Native SQL query alternative for database-specific features

### Service Layer
- **Data Provider**: Abstracts data loading
- **File Exporter**: Abstracts file operations
- **Report Service**: Orchestrates the workflow
- Clear separation of concerns
- Transaction management via `@Transactional`

### Job Layer
- Scheduled tasks using Spring's `@Scheduled`
- Delegates business logic to services
- Comprehensive error handling and logging

### Web Layer
- RESTful API endpoints
- Thin controllers (delegate to services)
- Proper HTTP status codes and content types

### File Layer
- File management abstraction
- Robust error handling with try-with-resources
- ATOMIC_MOVE for transactional file operations
- Comprehensive logging

## Configuration Management

### Externalized Configuration
- `@ConfigurationProperties` for type-safe configuration
- Profile-based properties (dev, prod, test)
- Environment-specific overrides
- No hardcoded values

### Profiles
- **dev**: H2 database, local file paths, verbose logging
- **prod**: SQL Server, production paths, minimal logging
- **test**: H2 in-memory, temp directories, disabled scheduling

## Testing Strategy

### Repository Tests (`@DataJpaTest`)
- H2 in-memory database
- Tests JPQL queries
- Verifies entity mappings
- Transaction rollback after each test

### Service Tests (Mockito)
- Mock repository and file dependencies
- Test business logic in isolation
- Test success and failure scenarios
- Verify exception handling

### File Manager Tests (`@TempDir`)
- Isolated file operations
- Test file creation, writing, moving
- Test archival logic
- Automatic cleanup

### Controller Tests (`@WebMvcTest`)
- Test web layer in isolation
- Mock service dependencies
- Verify HTTP responses
- Test JSON serialization

## Error Handling

### Exception Strategy
- Custom exception: `ReportGenerationException` for domain errors
- Catch and log all exceptions
- Move failed files to error directory
- Meaningful error messages for troubleshooting

### Failure Scenarios Handled
- Database connection failures
- File I/O errors
- Empty result sets
- Missing directories
- Permission errors

## File Management Strategy

### Directory Structure
```
/var/reports/employee-department/
  ├── temp/       (temporary files during generation)
  ├── output/     (successful reports)
  ├── error/      (failed reports)
  └── archive/    (old reports after retention period)
```

### File Lifecycle
1. Create temp file
2. Write data
3. On success: Move to output with timestamp
4. On failure: Move to error
5. After retention: Move to archive

### Atomic Operations
- Use `StandardCopyOption.ATOMIC_MOVE` where supported
- Fallback to standard move
- Ensures file consistency

## Scheduling

### Cron Configuration
- Expression: `"0 0 7 * * *"` (7:00 AM every day)
- Timezone: `"Europe/Berlin"` (CET/CEST)
- Spring's `@Scheduled` annotation
- `@EnableScheduling` on main application

### Execution Flow
1. Trigger at scheduled time
2. Load data from database
3. Generate CSV file
4. Move to output
5. Archive old files
6. Log success/failure

## REST API Design

### Endpoint Design
- RESTful resource-based URLs
- Proper HTTP methods (GET)
- Content negotiation (JSON, CSV)
- Appropriate status codes

### JSON Endpoint
- Returns `List<EmployeeDepartmentReportRow>`
- Spring MVC auto-serialization
- HTTP 200 on success, 500 on error

### CSV Download Endpoint
- Returns file as attachment
- Generates on-demand if needed
- Proper Content-Disposition headers
- HTTP 404 if no file available

## Database Design

### Entity Relationships
- One-to-Many: Department → Employees
- Many-to-One: Employee → Department
- Lazy loading for performance
- Cascade operations for data integrity

### Query Strategy
- JPQL for portability
- Native SQL alternative for specific features
- Constructor-based projection for DTOs
- Proper indexing recommendations (id, department_id)

## Extension Points

### Adding New Report Types
1. Create new DTO (e.g., `SalaryStatisticsRow`)
2. Add repository query method
3. Implement `ReportDataProvider` for new report
4. Optionally implement custom `ReportFileExporter`
5. Create new service implementation
6. Add new scheduled job
7. Add new REST endpoint

### Adding New Export Formats
1. Implement `ReportFileExporter` interface
2. Create format-specific file manager (e.g., `ExcelFileManager`)
3. Inject into service
4. Add REST endpoint for new format

### Adding Notifications
1. Create `NotificationService` interface
2. Implement email/SMS notification
3. Inject into job class
4. Call on success/failure

## Performance Considerations

### Database
- Read-only transactions (`@Transactional(readOnly = true)`)
- Lazy loading for associations
- Batch size configuration in properties
- Connection pooling (HikariCP by default)

### File Operations
- Buffered I/O for large files
- Try-with-resources for automatic cleanup
- Atomic moves for safety
- Stream-based file listing for archives

### Memory Management
- Constructor injection (immutable dependencies)
- No static state
- Proper resource cleanup
- Streaming for large datasets

## Security Considerations

### Database
- Parameterized queries (SQL injection prevention)
- Environment variables for credentials
- Encrypted connections (production)

### File System
- Restricted directory access
- No user-supplied file paths
- Proper file permissions

### API
- No sensitive data in logs
- Proper error messages (no stack traces to clients)
- Content-Type validation

## Monitoring and Logging

### Logging Strategy
- SLF4J with Logback
- Different levels per environment
- Contextual information in logs
- No sensitive data logged

### Log Levels
- ERROR: Failures that need attention
- WARN: Potential issues
- INFO: Important business events
- DEBUG: Detailed troubleshooting (dev only)

### Key Events Logged
- Scheduled job start/completion
- Report generation success/failure
- File operations (create, move, archive)
- Data loading metrics (row count)
- Exception details with context

## Deployment

### Packaging
- Spring Boot fat JAR
- Embedded Tomcat
- No external dependencies

### Configuration
- Externalized properties
- Environment-specific profiles
- Override via environment variables

### Database
- Schema creation: Hibernate DDL (dev)
- Schema validation: Manual SQL (prod)
- Sample data script provided

## Future Enhancements

### Potential Improvements
1. Email notifications on success/failure
2. Web dashboard for report history
3. Multiple report types
4. Excel export format
5. Report scheduling configuration UI
6. Advanced filtering options
7. Real-time WebSocket progress updates
8. Internationalization (i18n)
9. Report templates
10. Data encryption at rest

### Scalability
- Horizontal scaling (stateless design)
- Database read replicas
- Distributed file system (S3, Azure Blob)
- Message queue for job distribution
- Caching for frequently accessed data
