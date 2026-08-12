package com.healthcare.model;

/**
 * Lifecycle statuses for appointments.
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    WAITING("Waiting"),
    IN_CONSULTATION("In Consultation"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    AppointmentStatus(String label) {
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
