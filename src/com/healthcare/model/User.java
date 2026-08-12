package com.healthcare.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract User base class representing system actors.
 * Demonstrates Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    public User(String userId, String username, String password, String fullName, 
                String email, String phone, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Polymorphic method implemented by concrete subclasses to summarize user identity & duties.
     */
    public abstract String getRoleDescription();

    /**
     * Polymorphic authorization check method.
     */
    public boolean canManageAppointments() {
        return role == Role.PATIENT || role == Role.DOCTOR || role == Role.NURSE || role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return fullName + " (" + role.getDisplayName() + ")";
    }
}
