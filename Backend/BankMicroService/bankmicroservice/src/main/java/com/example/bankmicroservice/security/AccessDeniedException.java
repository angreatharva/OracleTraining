package com.example.bankmicroservice.security;

/**
 * Raised when an authenticated caller may not touch the resource they asked for.
 * Mapped to HTTP 403 by {@code GlobalExceptionHandler}.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
