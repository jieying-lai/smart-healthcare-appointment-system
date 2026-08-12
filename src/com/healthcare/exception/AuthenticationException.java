package com.healthcare.exception;

/**
 * Exception thrown during failed login or credential validation.
 */
public class AuthenticationException extends HealthcareException {
    public AuthenticationException(String message) {
        super(message);
    }
}
