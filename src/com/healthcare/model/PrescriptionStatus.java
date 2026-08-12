package com.healthcare.model;

/**
 * Status tracking for prescriptions.
 */
public enum PrescriptionStatus {
    PENDING("Prescription Pending"),
    PREPARING("Preparing Medication"),
    DISPENSED("Dispensed");

    private final String label;

    PrescriptionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
