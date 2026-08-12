package com.healthcare.exception;

/**
 * Exception thrown when validation fails for user records or transactions.
 */
public class InvalidRecordException extends HealthcareException {
    public InvalidRecordException(String message) {
        super(message);
    }
}
