package com.healthcare.model;

/**
 * Represents a Patient in the healthcare system.
 * Inherits from abstract User class.
 */
public class Patient extends User {
    private static final long serialVersionUID = 1L;

    private String dateOfBirth;
    private String bloodGroup;
    private String medicalHistory;
    private String emergencyContact;

    public Patient(String userId, String username, String password, String fullName, 
                   String email, String phone, String dateOfBirth, String bloodGroup, 
                   String medicalHistory, String emergencyContact) {
        super(userId, username, password, fullName, email, phone, Role.PATIENT);
        this.dateOfBirth = dateOfBirth;
        this.bloodGroup = bloodGroup;
        this.medicalHistory = medicalHistory;
        this.emergencyContact = emergencyContact;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String getRoleDescription() {
        return "Patient [Blood Group: " + (bloodGroup != null ? bloodGroup : "N/A") + "]";
    }
}
