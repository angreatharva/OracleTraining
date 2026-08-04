package com.example.portfoliomicroservice.security;

/** Raised when an authenticated caller may not touch the resource. Mapped to HTTP 403. */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
