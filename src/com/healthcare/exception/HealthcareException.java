package com.healthcare.exception;

/**
 * Base custom exception for Healthcare Application domain logic.
 */
public class HealthcareException extends Exception {
    public HealthcareException(String message) {
        super(message);
    }

    public HealthcareException(String message, Throwable cause) {
        super(message, cause);
    }
}
