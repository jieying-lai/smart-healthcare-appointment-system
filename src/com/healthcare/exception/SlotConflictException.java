package com.healthcare.exception;

/**
 * Exception thrown when booking an appointment that collides with an existing slot.
 */
public class SlotConflictException extends HealthcareException {
    public SlotConflictException(String message) {
        super(message);
    }
}
