package com.qashqade.extconn.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Source", schema = "qashqade")
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "SourceName", nullable = false, unique = true)
    private String sourceName;

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public String getSourceName()            { return sourceName; }
    public void setSourceName(String v)      { this.sourceName = v; }
}
