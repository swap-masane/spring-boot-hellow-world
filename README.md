# Employee-Department Report Application

A Spring Boot application that generates scheduled employee-department reports with automated file management and REST API access.

## Features

- **Scheduled Report Generation**: Runs daily at 7:00 AM CET
- **Database Integration**: Spring Data JPA with Hibernate, MS SQL Server (production), H2 (tests)
- **CSV Export**: Automated CSV generation with configurable retention and archival
- **REST API**: JSON and CSV endpoints for on-demand report access
- **File Management**: Automatic handling of temp/output/error/archive folders
- **SOLID Architecture**: Clean separation of concerns, interface-based design
- **Comprehensive Testing**: 25 tests with JUnit 5 and Mockito

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA with Hibernate
- MS SQL Server (production)
- H2 Database (testing)
- Maven
- JUnit 5 & Mockito

## Building and Running

### Prerequisites

- JDK 17 or higher
- Maven 3.6+
- MS SQL Server (for production) or H2 (embedded for dev/test)

### Build

```bash
mvn clean package
```

### Run Tests

```bash
mvn test
```

### Run Application

Development mode (H2):
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Production mode (SQL Server):
```bash
java -jar target/employee-department-report-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Configuration

Application properties are externalized in `application.properties` with profile-specific overrides:

- `application-dev.properties`: Development (H2 database)
- `application-prod.properties`: Production (SQL Server)
- `application-test.properties`: Testing (H2 in-memory)

Key configuration parameters:

```properties
report.base-directory=/var/reports/employee-department
report.time-zone=Europe/Berlin
report.output-retention=7d
```

## API Endpoints

### Get Report as JSON
```
GET /api/reports/employee-department
Content-Type: application/json
```

Returns employee-department report data as JSON array.

### Download Latest CSV
```
GET /api/reports/employee-department/csv
Content-Type: text/csv
```

Downloads the latest generated CSV report or generates one on-demand.

## Database Schema

### EMPLOYEE Table
- `id` (BIGINT, PK)
- `name` (VARCHAR)
- `salary` (DECIMAL)
- `department_id` (BIGINT, FK)

### DEPARTMENT Table
- `id` (BIGINT, PK)
- `name` (VARCHAR)

## Architecture

The application follows SOLID principles with clean separation of concerns:

- **config**: Configuration properties and beans
- **domain**: JPA entities and DTOs
- **repository**: Spring Data JPA repositories
- **service**: Business logic and orchestration
- **job**: Scheduled tasks
- **web**: REST controllers
- **file**: CSV file management

## Scheduled Job

The `EmployeeDepartmentReportJob` runs daily at 7:00 AM CET:

1. Loads employee-department data from database
2. Generates CSV file in temp directory
3. On success: Moves file to output with timestamp
4. On failure: Moves file to error directory
5. Archives old files based on retention policy

## Testing

- **Repository Tests**: H2 in-memory database with `@DataJpaTest`
- **Service Tests**: Mockito with `@ExtendWith(MockitoExtension.class)`
- **File Manager Tests**: JUnit 5 `@TempDir` for isolated file operations
- **Controller Tests**: Spring MVC Test with `@WebMvcTest`

Run all tests:
```bash
mvn test
```

## License

MIT License (see LICENSE file)
