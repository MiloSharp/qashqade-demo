package com.qashqade.extconn.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ReportType", schema = "qashqade")
public class ReportType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    @Column(name = "SortOrder")
    private Integer sortOrder;

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public Integer getSortOrder()              { return sortOrder; }
    public void setSortOrder(Integer s)        { this.sortOrder = s; }
}
