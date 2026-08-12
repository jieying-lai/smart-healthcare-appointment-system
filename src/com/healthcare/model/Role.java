package com.healthcare.model;

/**
 * Enumeration representing user roles within the healthcare management system.
 */
public enum Role {
    PATIENT("Patient"),
    DOCTOR("Doctor"),
    NURSE("Nurse"),
    PHARMACIST("Pharmacist"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
