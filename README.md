# qashqade Demo Application

A full-stack demo application built for qashqade AG, showcasing a Planner management system for private equity waterfall and carry software.

## Tech Stack

- **Backend:** Java 21 / Spring Boot 3.2 / Spring Data JPA / PostgreSQL
- **Frontend:** Angular 19 (standalone components) / TypeScript / SCSS
- **Build:** Maven (backend) / Angular CLI (frontend)
- **Containerisation:** Docker / docker-compose / nginx

## Quick Start (Docker)

The easiest way to run the application. Requires Docker Desktop.

    git clone https://github.com/MiloSharp/qashqade-demo.git
    cd qashqade-demo
    docker-compose up

Then open http://localhost in your browser.

This starts three containers:

    qashqade-postgres   PostgreSQL 17 with schema and sample data
    qashqade-backend    Spring Boot API on port 8080
    qashqade-frontend   Angular app served by nginx on port 80

To stop:

    docker-compose down

To stop and remove all data (full reset):

    docker-compose down -v

## Manual Setup (Development)

### Prerequisites

- Java 21
- Maven (on PATH)
- Node.js 18+ and npm
- PostgreSQL 14+
- Angular CLI: npm install -g @angular/cli

### Database Setup

Create a PostgreSQL database and user named qashqade, create schema qashqade inside it, then run the SQL migration files in database/ in this order:

    schema.sql
    add_auth_method.sql
    add_lookup_tables.sql
    add_trigger_columns.sql
    add_source_run_report.sql
    add_employee.sql
    add_fund.sql
    planner_sample_data.sql
    test_data.sql

### Backend Setup

Copy the example properties file and fill in your local database credentials:

    cp backend/src/main/resources/application-dev.properties.example \
       backend/src/main/resources/application-dev.properties

Then start the backend:

    cd backend
    mvn spring-boot:run

Backend starts on http://localhost:8080

### Frontend Setup

    cd frontend
    npm install
    ng serve

Frontend starts on http://localhost:4200

Note: When running manually, restore the localhost URLs in the service files.
The Docker build uses relative URLs for nginx proxying.

## Spring Boot Profiles

| Profile | SQL Logging | Database Config                                         |
|---------|-------------|---------------------------------------------------------|
| dev     | On          | Local (application-dev.properties)                      |
| prod    | Off         | Environment variables (DB_URL, DB_USERNAME, DB_PASSWORD)|

Default profile is dev. Override with -Dspring.profiles.active=prod

## Pages

- /connections - External Connection configuration management
- /planners - Planner management with Sources, Runs, Reports, Funds, and Employees

## Documentation

- DATABASE.md - Full database schema documentation with constraints, indexes, and JPA mapping
- PERFORMANCE.md - Performance analysis, Azure infrastructure recommendations, and security notes
