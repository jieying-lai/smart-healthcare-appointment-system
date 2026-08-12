package com.healthcare.model;

/**
 * Represents a Pharmacist user in the healthcare system.
 * Inherits from abstract User class.
 */
public class Pharmacist extends User {
    private static final long serialVersionUID = 1L;

    private String licenseNumber;
    private String pharmacySection;

    public Pharmacist(String userId, String username, String password, String fullName, 
                      String email, String phone, String licenseNumber, String pharmacySection) {
        super(userId, username, password, fullName, email, phone, Role.PHARMACIST);
        this.licenseNumber = licenseNumber;
        this.pharmacySection = pharmacySection;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPharmacySection() {
        return pharmacySection;
    }

    public void setPharmacySection(String pharmacySection) {
        this.pharmacySection = pharmacySection;
    }

    @Override
    public String getRoleDescription() {
        return "Pharmacist [Lic #: " + licenseNumber + ", Section: " + pharmacySection + "]";
    }
}
