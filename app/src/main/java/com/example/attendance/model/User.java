package com.example.attendance.model;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String fullname;
    private String empId;
    private String email;

    // Default constructor
    public User() {
    }

    // Constructor with fields
    public User(Long id, String fullname, String empId, String email) {
        this.id = id;
        this.fullname = fullname;
        this.empId = empId;
        this.email = email;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmpId() {
        return empId;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}