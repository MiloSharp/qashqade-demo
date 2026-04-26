package com.qashqade.extconn.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Planner", schema = "qashqade")
public class Planner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Description")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PlannerTypeId")
    private PlannerType plannerType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ExtConnId")
    private ExtConn extConn;

    @Column(name = "FundName")
    private String fundName;

    @Column(name = "FundAlias")
    private String fundAlias;

    @Column(name = "TriggerSources", nullable = false)
    private Boolean triggerSources = false;

    @Column(name = "TriggerRuns", nullable = false)
    private Boolean triggerRuns = false;

    @Column(name = "TriggerReports", nullable = false)
    private Boolean triggerReports = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OutputFormatId")
    private ReportType outputFormat;

    @Column(name = "ReportName")
    private String reportName;

    // Legacy free-text owner kept as fallback
    @Column(name = "Owner")
    private String owner;

    // New FK owner
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OwnerId")
    private Employee ownerEmployee;

    @Column(name = "Status", nullable = false)
    private String status = "";

    @Column(name = "StatusAt")
    private OffsetDateTime statusAt;

    @Column(name = "LogFile")
    private String logFile;

    @Column(name = "CreatedAt", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UpdatedAt")
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "PlannerSource", schema = "qashqade",
        joinColumns = @JoinColumn(name = "PlannerId"),
        inverseJoinColumns = @JoinColumn(name = "SourceId")
    )
    @OrderBy("sourceName ASC")
    private List<Source> sources = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "PlannerRun", schema = "qashqade",
        joinColumns = @JoinColumn(name = "PlannerId"),
        inverseJoinColumns = @JoinColumn(name = "RunId")
    )
    @OrderBy("runName ASC")
    private List<Run> runs = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "PlannerReport", schema = "qashqade",
        joinColumns = @JoinColumn(name = "PlannerId"),
        inverseJoinColumns = @JoinColumn(name = "ReportId")
    )
    @OrderBy("reportType ASC, reportName ASC")
    private List<Report> reports = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null)      this.createdAt = now;
        if (this.updatedAt == null)      this.updatedAt = now;
        if (this.status == null)         this.status = "";
        if (this.triggerSources == null) this.triggerSources = false;
        if (this.triggerRuns == null)    this.triggerRuns = false;
        if (this.triggerReports == null) this.triggerReports = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        if (this.status == null)         this.status = "";
        if (this.triggerSources == null) this.triggerSources = false;
        if (this.triggerRuns == null)    this.triggerRuns = false;
        if (this.triggerReports == null) this.triggerReports = false;
    }

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public String getDescription()                   { return description; }
    public void setDescription(String d)             { this.description = d; }
    public PlannerType getPlannerType()              { return plannerType; }
    public void setPlannerType(PlannerType pt)       { this.plannerType = pt; }
    public ExtConn getExtConn()                      { return extConn; }
    public void setExtConn(ExtConn ec)               { this.extConn = ec; }
    public String getFundName()                      { return fundName; }
    public void setFundName(String v)                { this.fundName = v; }
    public String getFundAlias()                     { return fundAlias; }
    public void setFundAlias(String v)               { this.fundAlias = v; }
    public Boolean getTriggerSources()               { return triggerSources; }
    public void setTriggerSources(Boolean v)         { this.triggerSources = v != null ? v : false; }
    public Boolean getTriggerRuns()                  { return triggerRuns; }
    public void setTriggerRuns(Boolean v)            { this.triggerRuns = v != null ? v : false; }
    public Boolean getTriggerReports()               { return triggerReports; }
    public void setTriggerReports(Boolean v)         { this.triggerReports = v != null ? v : false; }
    public ReportType getOutputFormat()              { return outputFormat; }
    public void setOutputFormat(ReportType v)        { this.outputFormat = v; }
    public String getReportName()                    { return reportName; }
    public void setReportName(String v)              { this.reportName = v; }
    public String getOwner()                         { return owner; }
    public void setOwner(String v)                   { this.owner = v; }
    public Employee getOwnerEmployee()               { return ownerEmployee; }
    public void setOwnerEmployee(Employee v)         { this.ownerEmployee = v; }
    public String getStatus()                        { return status; }
    public void setStatus(String v)                  { this.status = v != null ? v : ""; }
    public OffsetDateTime getStatusAt()              { return statusAt; }
    public void setStatusAt(OffsetDateTime v)        { this.statusAt = v; }
    public String getLogFile()                       { return logFile; }
    public void setLogFile(String v)                 { this.logFile = v; }
    public OffsetDateTime getCreatedAt()             { return createdAt; }
    public OffsetDateTime getUpdatedAt()             { return updatedAt; }
    public List<Source> getSources()                 { return sources; }
    public void setSources(List<Source> v)           { this.sources = v; }
    public List<Run> getRuns()                       { return runs; }
    public void setRuns(List<Run> v)                 { this.runs = v; }
    public List<Report> getReports()                 { return reports; }
    public void setReports(List<Report> v)           { this.reports = v; }
}
