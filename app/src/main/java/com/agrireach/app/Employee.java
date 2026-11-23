package com.agrireach.app;
public class Employee {
    private String employeeCode;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String username;
    public Employee(
            String employeeCode,
            String fullName,
            String phoneNumber,
            String email,
            String username) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
    }
    public String getEmployeeCode() {
        return employeeCode;
    }
    public String getFullName() {
        return fullName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
}