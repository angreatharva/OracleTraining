package com.example.wtms.Exceptions;

/**
 * Custom exception for Role-related operations.
 */
public class RolesException extends RuntimeException {

    public RolesException() {
        super();
    }

    public RolesException(String message) {
        super(message);
    }

    public RolesException(String message, Throwable cause) {
        super(message, cause);
    }

    public RolesException(Throwable cause) {
        super(cause);
    }
}