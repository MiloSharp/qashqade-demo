package com.qashqade.extconn.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Fund", schema = "qashqade")
public class Fund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FundName", nullable = false, unique = true)
    private String fundName;

    @OneToMany(mappedBy = "fund", fetch = FetchType.EAGER,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("aliasName ASC")
    private List<FundAlias> aliases = new ArrayList<>();

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getFundName()                  { return fundName; }
    public void setFundName(String v)            { this.fundName = v; }
    public List<FundAlias> getAliases()          { return aliases; }
    public void setAliases(List<FundAlias> v)    { this.aliases = v; }
}
