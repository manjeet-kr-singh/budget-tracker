# Budget Tracker

Daily Budget Tracker and Expense Planner application built with Spring Boot.

## Features

- **Dashboard**: Overview of your financial status.
- **Transactions**: Add, edit, and delete income and expense records.
- **Import/Export**: 
  - Import transactions from Excel.
  - Export data to Excel/CSV.
- **Reports**: Visualize your spending habits.
- **Budget Planning**: Set and track budgets.

## Technology Stack

- **Java**: JDK 1.8
- **Framework**: Spring Boot 2.7.18
- **Build Tool**: Maven
- **Database**: H2 In-Memory Database
- **Template Engine**: Thymeleaf
- **Other**: Lombok, Apache POI (for Excel processing)

## Prerequisites

- Java Development Kit (JDK) 8 installed.

## Getting Started

### Quick Start (Windows)

The project includes a PowerShell script to automatically setup Maven and run the application.

1. Open a PowerShell terminal in the project root.
2. Run the script:
   ```powershell
   .\run_with_maven.ps1
   ```
   This script will:
   - Check for a local Maven installation.
   - Download a portable Maven version if one isn't found.
   - Build and start the Spring Boot application.

### Manual Setup

If you have Maven installed globally:

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```bash
   cd budget-tracker
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Usage

Once the application is running, open your web browser and go to:

**http://localhost:8080**

### Database Console

To access the H2 in-memory database console:

- **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Driver Class**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:mem:budget_tracker`
- **User Name**: `sa`
- **Password**: `password`
