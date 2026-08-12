package com.healthcare.model;

/**
 * Represents an Administrator in the healthcare system.
 * Inherits from abstract User class.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String accessLevel;

    public Admin(String userId, String username, String password, String fullName, 
                 String email, String phone, String accessLevel) {
        super(userId, username, password, fullName, email, phone, Role.ADMIN);
        this.accessLevel = accessLevel;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator [Access Level: " + accessLevel + "]";
    }
}
