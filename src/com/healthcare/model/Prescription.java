package com.healthcare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model for Prescriptions issued by doctors and dispensed by pharmacists.
 * Tracks prescribing doctor as well as dispensing pharmacist & pharmacy section/counter.
 */
public class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String prescriptionId;
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String medicationName;
    private String dosage;
    private String instructions;
    private PrescriptionStatus status;
    private LocalDateTime issuedAt;

    // Dispensing Pharmacist & Pharmacy Section Details
    private String dispensedByPharmacistId;
    private String dispensedByPharmacistName;
    private String pharmacySection;

    public Prescription(String prescriptionId, String appointmentId, String patientId, 
                        String patientName, String doctorId, String doctorName, 
                        String medicationName, String dosage, String instructions) {
        this.prescriptionId = prescriptionId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.instructions = instructions;
        this.status = PrescriptionStatus.PENDING;
        this.issuedAt = LocalDateTime.now();
        this.dispensedByPharmacistId = "-";
        this.dispensedByPharmacistName = "Pending Dispensing";
        this.pharmacySection = "Central Pharmacy";
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public String getFormattedIssuedAt() {
        return issuedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getDispensedByPharmacistId() {
        return dispensedByPharmacistId;
    }

    public void setDispensedByPharmacistId(String dispensedByPharmacistId) {
        this.dispensedByPharmacistId = dispensedByPharmacistId;
    }

    public String getDispensedByPharmacistName() {
        return dispensedByPharmacistName;
    }

    public void setDispensedByPharmacistName(String dispensedByPharmacistName) {
        this.dispensedByPharmacistName = dispensedByPharmacistName;
    }

    public String getPharmacySection() {
        return pharmacySection;
    }

    public void setPharmacySection(String pharmacySection) {
        this.pharmacySection = pharmacySection;
    }
}
