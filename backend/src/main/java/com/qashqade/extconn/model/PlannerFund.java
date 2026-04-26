package com.qashqade.extconn.model;

import jakarta.persistence.*;

@Entity
@Table(name = "PlannerFund", schema = "qashqade")
public class PlannerFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlannerId", nullable = false)
    private Planner planner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FundId", nullable = false)
    private Fund fund;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AliasId")
    private FundAlias alias;

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Planner getPlanner()                  { return planner; }
    public void setPlanner(Planner planner)      { this.planner = planner; }
    public Fund getFund()                        { return fund; }
    public void setFund(Fund fund)               { this.fund = fund; }
    public FundAlias getAlias()                  { return alias; }
    public void setAlias(FundAlias alias)        { this.alias = alias; }
}
