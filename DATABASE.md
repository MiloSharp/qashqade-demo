# Database Documentation

## Overview

- **Database:** PostgreSQL 14+
- **Schema:** `qashqade`
- **ORM:** Spring Data JPA / Hibernate
- **Naming convention:** Table and column names use PascalCase with quoted identifiers (`globally_quoted_identifiers=true`) to preserve casing in PostgreSQL

---

## Entity Relationship Summary

```
ExtConn ──< Planner >── PlannerType
                │
                ├──< PlannerSource >── Source
                ├──< PlannerRun    >── Run
                ├──< PlannerReport >── Report
                ├──< PlannerFund   >── Fund ──< FundAlias
                └──── Employee (Owner)
                └──── ReportType (OutputFormat)
```

---

## Tables

### `ExtConn`
External API connection configurations consumed by Planners.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | Auto-generated |
| Name | VARCHAR(255) | NOT NULL | Display name |
| BaseUrl | VARCHAR(500) | | API base URL |
| AuthLocation | VARCHAR(50) | | Header or QueryParameters |
| AuthKey | VARCHAR(255) | | API key name / header name |
| AuthValue | VARCHAR(500) | | Secret value — masked in UI once set |
| AuthMethod | VARCHAR(50) | | API Key, OAuth 2.0, Basic Auth, Bearer Token, JWT, HMAC, SAML, No Auth |
| Description | TEXT | | Free text |
| CreatedAt | TIMESTAMPTZ | NOT NULL | Set by @PrePersist |
| UpdatedAt | TIMESTAMPTZ | NOT NULL | Set by @PreUpdate |

**Indexes:**
```sql
CREATE INDEX idx_extconn_name ON qashqade."ExtConn" ((lower("Name")));
```

**JPA Mapping:** `ExtConn.java` — `@Entity`, `@Table(name="ExtConn", schema="qashqade")`

---

### `PlannerType`
Lookup table for planner categories.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| Name | VARCHAR(100) | NOT NULL, UNIQUE | NAV, Carry, Waterfall, Statement, Fees, Allocation, Benchmark, Cash Flow, Risk, Custom |
| SortOrder | INT | | Controls dropdown display order |

**JPA Mapping:** `PlannerType.java` — loaded via `PlannerTypeRepository.findAllByOrderBySortOrderAsc()`

---

### `ReportType`
Lookup table for output file formats.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| Name | VARCHAR(100) | NOT NULL, UNIQUE | PDF, Excel, CSV, JSON, HTML, Word |
| SortOrder | INT | | Controls dropdown display order |

**JPA Mapping:** `ReportType.java` — used as `OutputFormat` FK on Planner

---

### `Employee`
Staff members who can be assigned as Planner owners.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| EmployeeId | VARCHAR(20) | UNIQUE | Optional employee reference code |
| FullName | VARCHAR(200) | NOT NULL | Used in typeahead search |
| Email | VARCHAR(200) | UNIQUE | |
| Title | VARCHAR(100) | | Job title |
| Department | VARCHAR(100) | | Department name |
| DateOfBirth | DATE | | |

**Indexes:**
```sql
CREATE INDEX idx_employee_fullname ON qashqade."Employee" ((lower("FullName")));
```

**JPA Mapping:** `Employee.java` — referenced by `Planner.ownerEmployee` as `@ManyToOne`

---

### `Fund`
Shared reference data for fund names used across Planners.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| FundName | VARCHAR(255) | NOT NULL, UNIQUE | |

**Indexes:**
```sql
CREATE INDEX idx_fund_name ON qashqade."Fund" ((lower("FundName")));
```

**JPA Mapping:** `Fund.java` — has `@OneToMany` to `FundAlias`

---

### `FundAlias`
Zero-to-many aliases per Fund. Aliases are global (shared across all Planners).

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| FundId | BIGINT | NOT NULL, FK → Fund(id) CASCADE DELETE | |
| AliasName | VARCHAR(255) | NOT NULL | UNIQUE per Fund |

**Constraints:**
```sql
UNIQUE ("FundId", "AliasName")
```

**Indexes:**
```sql
CREATE INDEX idx_fundalias_fund ON qashqade."FundAlias" ("FundId");
CREATE INDEX idx_fundalias_name ON qashqade."FundAlias" ((lower("AliasName")));
```

**JPA Mapping:** `FundAlias.java` — `@ManyToOne` to Fund, `@JsonIgnore` on fund field to prevent circular serialisation

---

### `Source`
Shared reference data for data source names.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| SourceName | VARCHAR(255) | NOT NULL, UNIQUE | e.g. "Data source - Bloomberg" |

**Indexes:**
```sql
CREATE INDEX idx_source_name ON qashqade."Source" ((lower("SourceName")));
```

---

### `Run`
Shared reference data for run names.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| RunName | VARCHAR(255) | NOT NULL, UNIQUE | e.g. "Carried interest - Realized" |

**Indexes:**
```sql
CREATE INDEX idx_run_name ON qashqade."Run" ((lower("RunName")));
```

---

### `Report`
Shared reference data combining a report category (type) and a specific report name.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| ReportType | VARCHAR(100) | NOT NULL | Category e.g. "Management Reports", "Investor Reports" |
| ReportName | VARCHAR(255) | NOT NULL | e.g. "01 - Standard report per LP" |

**Constraints:**
```sql
UNIQUE ("ReportType", "ReportName")
```

**Indexes:**
```sql
CREATE INDEX idx_report_type_name ON qashqade."Report" ((lower("ReportType")), (lower("ReportName")));
```

---

### `Planner`
Core entity. Represents a configured automation planner.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| Name | VARCHAR(255) | NOT NULL | |
| Description | TEXT | | |
| PlannerTypeId | BIGINT | FK → PlannerType(id) SET NULL | |
| ExtConnId | BIGINT | FK → ExtConn(id) SET NULL | |
| OutputFormatId | BIGINT | FK → ReportType(id) SET NULL | File output format |
| OwnerId | BIGINT | FK → Employee(id) SET NULL | |
| TriggerSources | BOOLEAN | NOT NULL DEFAULT false | |
| TriggerRuns | BOOLEAN | NOT NULL DEFAULT false | |
| TriggerReports | BOOLEAN | NOT NULL DEFAULT false | |
| Status | VARCHAR(50) | NOT NULL DEFAULT '' | '', 'Finished', 'Failed' |
| StatusAt | TIMESTAMPTZ | | Auto-set by server when Status changes |
| LogFile | TEXT | | Run output log |
| CreatedAt | TIMESTAMPTZ | NOT NULL | Set by @PrePersist |
| UpdatedAt | TIMESTAMPTZ | NOT NULL | Set by @PreUpdate |
| Owner | VARCHAR(255) | | Legacy text field — to be dropped after migration verified |
| FundName | VARCHAR(255) | | Legacy — to be dropped |
| FundAlias | VARCHAR(255) | | Legacy — to be dropped |
| SourceName | VARCHAR(255) | | Legacy — to be dropped |
| RunName | VARCHAR(255) | | Legacy — to be dropped |
| ReportName | VARCHAR(255) | | Legacy — to be dropped |

**Indexes:**
```sql
CREATE INDEX idx_planner_owner    ON qashqade."Planner" ((lower("Owner")));
CREATE INDEX idx_planner_status   ON qashqade."Planner" ("Status");
CREATE INDEX idx_planner_statusat ON qashqade."Planner" ("StatusAt" DESC);
CREATE INDEX idx_planner_extconnid      ON qashqade."Planner" ("ExtConnId");
CREATE INDEX idx_planner_plannertypeid  ON qashqade."Planner" ("PlannerTypeId");
CREATE INDEX idx_planner_outputformatid ON qashqade."Planner" ("OutputFormatId");
CREATE INDEX idx_planner_ownerid        ON qashqade."Planner" ("OwnerId");
```

**JPA Mapping:** `Planner.java`
- `@PrePersist` sets `CreatedAt`, `UpdatedAt`, defaults for booleans and Status
- `@PreUpdate` sets `UpdatedAt`, guards against null Status/booleans
- `@ManyToMany` collections for Sources, Runs, Reports via junction tables
- `@ManyToOne EAGER` for PlannerType, ExtConn, OutputFormat, OwnerEmployee
- Status auto-timestamps handled in `PlannerService.update()` — compares old vs new status value

---

### Junction Tables

#### `PlannerSource`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| PlannerId | BIGINT | NOT NULL, FK → Planner(id) CASCADE DELETE |
| SourceId | BIGINT | NOT NULL, FK → Source(id) CASCADE DELETE |

**Constraints:** `UNIQUE (PlannerId, SourceId)`

---

#### `PlannerRun`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| PlannerId | BIGINT | NOT NULL, FK → Planner(id) CASCADE DELETE |
| RunId | BIGINT | NOT NULL, FK → Run(id) CASCADE DELETE |

**Constraints:** `UNIQUE (PlannerId, RunId)`

---

#### `PlannerReport`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PK |
| PlannerId | BIGINT | NOT NULL, FK → Planner(id) CASCADE DELETE |
| ReportId | BIGINT | NOT NULL, FK → Report(id) CASCADE DELETE |

**Constraints:** `UNIQUE (PlannerId, ReportId)`

---

#### `PlannerFund`
| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| id | BIGSERIAL | PK | |
| PlannerId | BIGINT | NOT NULL, FK → Planner(id) CASCADE DELETE | |
| FundId | BIGINT | NOT NULL, FK → Fund(id) CASCADE DELETE | |
| AliasId | BIGINT | FK → FundAlias(id) SET NULL | Optional — which alias to use for this association |

**Note:** No UNIQUE constraint on (PlannerId, FundId) — the same fund can appear multiple times on a planner with different aliases. This is intentional to support fund/alias tokens in report output.

**Indexes:**
```sql
CREATE INDEX idx_plannerfund_planner ON qashqade."PlannerFund" ("PlannerId");
CREATE INDEX idx_plannerfund_fund    ON qashqade."PlannerFund" ("FundId");
```

---

## Spring JPA Architecture

### Repository Layer
All repositories extend `JpaRepository<Entity, Long>` providing standard CRUD. Custom queries use `@Query` with JPQL:

```java
@Query("SELECT p FROM Planner p WHERE " +
       "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(p.owner) LIKE LOWER(CONCAT('%', :search, '%'))")
Page<Planner> findByNameOrOwnerContainingIgnoreCase(
    @Param("search") String search, Pageable pageable);
```

### Service Layer
- `PlannerService` — handles all business logic including status auto-timestamping, FK resolution, and sort direction mapping
- `ExtConnService` — handles pagination and search for External Connections
- Sort safety: `sortBy` values are validated via a `switch` statement before being passed to JPA to prevent SQL injection via sort parameters

### Pagination
All list endpoints accept `page`, `size`, `search`, `sort`, and `dir` parameters. Page size is capped at 100 server-side via `Math.min(size, 100)`. Results are returned as Spring `Page<T>` objects containing `content`, `totalElements`, and `totalPages`.

---

## Performance Considerations

### Current Optimisations
- All search queries use `LOWER()` functional indexes to enable case-insensitive search without sequential scans
- FK columns are indexed to speed up JOINs
- Server-side pagination prevents large result sets reaching the client
- Page size capped at 100 to prevent accidental full-table fetches

### Known Limitations
- `@ManyToOne(fetch = FetchType.EAGER)` on Planner causes multiple SELECT statements per row (N+1 pattern). Recommended fix: use `@EntityGraph` or `JOIN FETCH` in the repository query to load all associations in a single SQL statement
- `LogFile` stored as TEXT inline in the Planner table — at scale this should move to a separate table or blob storage
- `@ManyToMany` with `EAGER` fetch on Sources, Runs, Reports loads all associations for every Planner in a page — consider lazy loading with explicit fetch when expanding a row

### Recommended Future Indexes
```sql
-- Composite index for name+owner search
CREATE INDEX idx_planner_name_owner
    ON qashqade."Planner" ((lower("Name")), (lower("Owner")));
```
