package com.qashqade.extconn.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Report", schema = "qashqade")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ReportType", nullable = false)
    private String reportType;

    @Column(name = "ReportName", nullable = false)
    private String reportName;

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public String getReportType()            { return reportType; }
    public void setReportType(String v)      { this.reportType = v; }
    public String getReportName()            { return reportName; }
    public void setReportName(String v)      { this.reportName = v; }

    // Display label for typeahead
    public String getLabel() {
        return reportType + " — " + reportName;
    }
}
