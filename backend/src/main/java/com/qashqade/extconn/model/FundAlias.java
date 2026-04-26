package com.qashqade.extconn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "FundAlias", schema = "qashqade")
public class FundAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FundId", nullable = false)
    @JsonIgnore
    private Fund fund;

    @Column(name = "AliasName", nullable = false)
    private String aliasName;

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public Fund getFund()                    { return fund; }
    public void setFund(Fund fund)           { this.fund = fund; }
    public String getAliasName()             { return aliasName; }
    public void setAliasName(String v)       { this.aliasName = v; }
}
