package com.qashqade.extconn.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Run", schema = "qashqade")
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "RunName", nullable = false, unique = true)
    private String runName;

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }
    public String getRunName()           { return runName; }
    public void setRunName(String v)     { this.runName = v; }
}
