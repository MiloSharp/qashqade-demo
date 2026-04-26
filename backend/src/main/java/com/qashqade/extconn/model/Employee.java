package com.qashqade.extconn.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Employee", schema = "qashqade")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EmployeeId", unique = true)
    private String employeeId;

    @Column(name = "FullName", nullable = false)
    private String fullName;

    @Column(name = "Email", unique = true)
    private String email;

    @Column(name = "Title")
    private String title;

    @Column(name = "Department")
    private String department;

    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getEmployeeId()                { return employeeId; }
    public void setEmployeeId(String v)          { this.employeeId = v; }
    public String getFullName()                  { return fullName; }
    public void setFullName(String v)            { this.fullName = v; }
    public String getEmail()                     { return email; }
    public void setEmail(String v)               { this.email = v; }
    public String getTitle()                     { return title; }
    public void setTitle(String v)               { this.title = v; }
    public String getDepartment()                { return department; }
    public void setDepartment(String v)          { this.department = v; }
    public LocalDate getDateOfBirth()            { return dateOfBirth; }
    public void setDateOfBirth(LocalDate v)      { this.dateOfBirth = v; }
}
