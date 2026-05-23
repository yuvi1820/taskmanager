package com.taskmanager.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String role;
    private String passwordHash;
    private String email;
    private Integer taskCount;
    private String status;
    private String otp;
    private Timestamp otpExpiration;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Timestamp getOtpExpiration() {
        return otpExpiration;
    }

    public void setOtpExpiration(Timestamp otpExpiration) {
        this.otpExpiration = otpExpiration;
    }

    public boolean isManager() {
        return "manager".equals(role);
    }

    public boolean isEmployee() {
        return "employee".equals(role);
    }
}
