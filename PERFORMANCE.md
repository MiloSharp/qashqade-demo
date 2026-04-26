# Performance Notes & Infrastructure Recommendations

## Current Performance Baseline

The application was tested with the following data volumes:

| Table | Row Count |
|-------|-----------|
| Planner | 1,000+ |
| ExtConn | 100 |
| Source | 15 |
| Run | 20 |
| Report | 24 |
| Employee | 9 |
| Fund | 11 |
| FundAlias | 16 |

All list endpoints respond within acceptable time at these volumes with server-side pagination active.

---

## Identified Bottlenecks

### 1. N+1 Query Problem
**Severity: High at scale**

The `Planner` entity uses `FetchType.EAGER` on all `@ManyToOne` relationships (`PlannerType`, `ExtConn`, `ReportType`, `Employee`) and `@ManyToMany` for Sources, Runs, and Reports. This means a page of 25 planners may generate 100+ SELECT statements — one for the page, then additional queries per row for each relationship.

**Fix:** Replace EAGER fetching with a single JOIN FETCH query in the repository:
```java
@Query("SELECT DISTINCT p FROM Planner p " +
       "LEFT JOIN FETCH p.plannerType " +
       "LEFT JOIN FETCH p.extConn " +
       "LEFT JOIN FETCH p.ownerEmployee " +
       "LEFT JOIN FETCH p.sources " +
       "LEFT JOIN FETCH p.runs " +
       "LEFT JOIN FETCH p.reports " +
       "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
       "OR LOWER(p.owner) LIKE LOWER(CONCAT('%', :search, '%'))")
Page<Planner> findAllWithAssociations(@Param("search") String search, Pageable pageable);
```
Expected improvement: reduce 100+ queries per page load to 1-3.

### 2. LogFile Inline Storage
**Severity: Medium at scale**

The `LogFile` column stores run output as TEXT directly in the `Planner` table. At 100,000 planners with 500KB logs each, this adds ~50GB to the primary table, slowing all queries that scan it.

**Fix:** Move LogFile to a separate `PlannerLog` table:
```sql
CREATE TABLE qashqade."PlannerLog" (
    id         BIGSERIAL PRIMARY KEY,
    "PlannerId" BIGINT NOT NULL REFERENCES qashqade."Planner"(id) ON DELETE CASCADE,
    "LogFile"  TEXT,
    "CreatedAt" TIMESTAMPTZ DEFAULT NOW()
);
```
Fetch only when the user clicks Download.

### 3. Missing Composite Index for Search
**Severity: Medium**

The current search query uses OR across `Name` and `Owner`. PostgreSQL cannot use a single index for OR conditions efficiently.

**Fix:**
```sql
CREATE INDEX idx_planner_name_owner
    ON qashqade."Planner" (lower("Name"), lower("Owner"));
```
Or consider a full-text search index using `tsvector` for more sophisticated search:
```sql
ALTER TABLE qashqade."Planner"
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        to_tsvector('english', coalesce("Name", '') || ' ' || coalesce("Owner", ''))
    ) STORED;

CREATE INDEX idx_planner_fts ON qashqade."Planner" USING GIN (search_vector);
```

### 4. Angular Change Detection
**Severity: Low (frontend)**

The application uses `ChangeDetectorRef` with manual `detectChanges()` calls throughout rather than Angular's default zone-based detection. This was necessary due to `async/await` fetch functions breaking out of Angular's NgZone. A cleaner long-term solution would be to migrate to Angular Signals (available in Angular 17+) which are zone-agnostic by design.

---

## Recommended Infrastructure

### For a Production Deployment on Azure

```
┌─────────────────────────────────────────────┐
│           Azure Kubernetes Service           │
│                                             │
│  ┌──────────────┐    ┌──────────────────┐  │
│  │   nginx pod  │    │  Spring Boot pod  │  │
│  │  (Angular)   │───▶│   (Java API)     │  │
│  │  2 replicas  │    │   2 replicas     │  │
│  └──────────────┘    └────────┬─────────┘  │
│                               │             │
└───────────────────────────────┼─────────────┘
                                │
                    ┌───────────▼───────────┐
                    │  Azure Database for   │
                    │  PostgreSQL Flexible  │
                    │  Server (managed)     │
                    └───────────────────────┘
```

**Specific Azure services:**

| Component | Azure Service | Reason |
|-----------|--------------|--------|
| Container orchestration | Azure Kubernetes Service (AKS) | Managed Kubernetes, auto-scaling |
| Frontend | nginx container in AKS | Serves Angular static files |
| Backend | Spring Boot container in AKS | Java API |
| Database | Azure Database for PostgreSQL Flexible Server | Managed PostgreSQL, automatic backups, high availability |
| Secrets | Azure Key Vault + AKS CSI Driver | Never store credentials in code or config |
| Container registry | Azure Container Registry (ACR) | Private image storage |
| Ingress | Azure Application Gateway / nginx ingress | SSL termination, routing |
| Monitoring | Azure Monitor + Application Insights | Logs, metrics, alerts |

---

## Security Recommendations for Azure

### 1. Secrets Management
Never store database credentials in `application.properties` or Kubernetes ConfigMaps. Use Azure Key Vault:
- Store `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` in Key Vault
- Use the AKS CSI Secret Store driver to mount secrets as environment variables at pod startup
- Rotate secrets without redeploying the application

### 2. Network Security
- Deploy AKS in a private Virtual Network (VNet)
- Database server should have no public endpoint — only accessible from within the VNet via private link
- Use Network Security Groups (NSGs) to restrict traffic between pods
- Enable Azure DDoS Protection on the Application Gateway

### 3. Identity & Access
- Use Azure Managed Identity for the AKS pod to access Key Vault — no service principal credentials needed
- Enable Azure Active Directory integration for AKS
- Apply least-privilege RBAC to all service accounts

### 4. Container Security
- Scan container images with Microsoft Defender for Containers before deployment
- Use distroless or minimal base images (e.g. `eclipse-temurin:21-jre-alpine` for Java)
- Run containers as non-root users
- Set resource limits on all pods to prevent runaway resource consumption

### 5. Data Protection
- Enable encryption at rest on Azure Database for PostgreSQL (enabled by default)
- Enable SSL/TLS for all database connections (`spring.datasource.url` should include `?sslmode=require`)
- Enable Azure Backup for the database with geo-redundant storage
- Implement GDPR-compliant data retention policies for Employee PII fields

### 6. API Security
- Add Spring Security to the backend with JWT authentication
- Enable HTTPS only — redirect HTTP to HTTPS at the Application Gateway
- Implement rate limiting at the Application Gateway level
- Add CORS configuration to restrict allowed origins to the known frontend domain

---

## Estimated Azure Costs (Monthly, Production)

| Service | SKU | Estimated Cost |
|---------|-----|---------------|
| AKS (2 node pool, Standard_D2s_v3) | Standard | ~$140 |
| Azure Database for PostgreSQL Flexible | Standard_D2ds_v4, 32GB | ~$130 |
| Azure Container Registry | Basic | ~$5 |
| Application Gateway | Small, 2 instances | ~$80 |
| Azure Key Vault | Standard | ~$5 |
| Azure Monitor | Pay per GB | ~$20 |
| **Total** | | **~$380/month** |

*Costs are estimates and vary by region. Development/staging environments can use burstable SKUs to reduce costs by ~60%.*
