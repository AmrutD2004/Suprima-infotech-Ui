// RegisterRequest.java
package com.example.attendance.model;

public class RegisterRequest {
    private String fullname;
    private String empId;
    private String email;
    private String password;

    // Constructor
    public RegisterRequest(String fullname, String empId, String email, String password) {
        this.fullname = fullname;
        this.empId = empId;
        this.email = email;
        this.password = password;
    }

    // Getters (needed for serialization)
    public String getFullname() { return fullname; }
    public String getEmpId() { return empId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}