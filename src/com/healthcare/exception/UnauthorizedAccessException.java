package com.healthcare.exception;

/**
 * Exception thrown when a user attempts an action without sufficient permissions.
 */
public class UnauthorizedAccessException extends HealthcareException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
