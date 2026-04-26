package com.qashqade.extconn.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ExtConn", schema = "qashqade")
public class ExtConn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "BaseUrl")
    private String baseUrl;

    @Column(name = "AuthLocation")
    private String authLocation;

    @Column(name = "AuthKey")
    private String authKey;

    @Column(name = "AuthValue")
    private String authValue;

    @Column(name = "AuthMethod")
    private String authMethod;

    @Column(name = "Description")
    private String description;

    @Column(name = "CreatedAt", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "UpdatedAt")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public String getBaseUrl()                       { return baseUrl; }
    public void setBaseUrl(String v)                 { this.baseUrl = v; }
    public String getAuthLocation()                  { return authLocation; }
    public void setAuthLocation(String v)            { this.authLocation = v; }
    public String getAuthKey()                       { return authKey; }
    public void setAuthKey(String v)                 { this.authKey = v; }
    public String getAuthValue()                     { return authValue; }
    public void setAuthValue(String v)               { this.authValue = v; }
    public String getAuthMethod()                    { return authMethod; }
    public void setAuthMethod(String v)              { this.authMethod = v; }
    public String getDescription()                   { return description; }
    public void setDescription(String v)             { this.description = v; }
    public OffsetDateTime getCreatedAt()             { return createdAt; }
    public OffsetDateTime getUpdatedAt()             { return updatedAt; }
}
