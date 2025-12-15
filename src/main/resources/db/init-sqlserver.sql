-- SQL Server Database Initialization Script
-- Employee-Department Report Application

-- Create database (if needed)
-- CREATE DATABASE employee_reports;
-- GO

-- Use the database
USE employee_reports;
GO

-- Create DEPARTMENT table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DEPARTMENT')
BEGIN
    CREATE TABLE DEPARTMENT (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(255) NOT NULL
    );
END
GO

-- Create EMPLOYEE table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'EMPLOYEE')
BEGIN
    CREATE TABLE EMPLOYEE (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        salary DECIMAL(19,2) NOT NULL,
        department_id BIGINT NOT NULL,
        CONSTRAINT FK_Employee_Department FOREIGN KEY (department_id) 
            REFERENCES DEPARTMENT(id)
    );
END
GO

-- Sample data for testing
-- Insert departments
INSERT INTO DEPARTMENT (name) VALUES ('Engineering');
INSERT INTO DEPARTMENT (name) VALUES ('Sales');
INSERT INTO DEPARTMENT (name) VALUES ('Human Resources');
INSERT INTO DEPARTMENT (name) VALUES ('Marketing');
INSERT INTO DEPARTMENT (name) VALUES ('Finance');
GO

-- Insert employees
DECLARE @engId BIGINT = (SELECT id FROM DEPARTMENT WHERE name = 'Engineering');
DECLARE @salesId BIGINT = (SELECT id FROM DEPARTMENT WHERE name = 'Sales');
DECLARE @hrId BIGINT = (SELECT id FROM DEPARTMENT WHERE name = 'Human Resources');
DECLARE @mktId BIGINT = (SELECT id FROM DEPARTMENT WHERE name = 'Marketing');
DECLARE @finId BIGINT = (SELECT id FROM DEPARTMENT WHERE name = 'Finance');

INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Alice Johnson', 75000.00, @engId);
INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Bob Smith', 85000.00, @engId);
INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Charlie Brown', 90000.00, @engId);

INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('David Lee', 65000.00, @salesId);
INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Emma Wilson', 70000.00, @salesId);

INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Frank Martinez', 60000.00, @hrId);
INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Grace Davis', 62000.00, @hrId);

INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Henry Garcia', 68000.00, @mktId);
INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Ivy Rodriguez', 72000.00, @mktId);

INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Jack Anderson', 80000.00, @finId);
INSERT INTO EMPLOYEE (name, salary, department_id) VALUES ('Karen Thomas', 85000.00, @finId);
GO

-- Verify data
SELECT 
    e.name AS EmployeeName,
    d.name AS DepartmentName,
    e.salary AS Salary
FROM 
    EMPLOYEE e
    INNER JOIN DEPARTMENT d ON e.department_id = d.id
ORDER BY 
    d.name, e.name;
GO
