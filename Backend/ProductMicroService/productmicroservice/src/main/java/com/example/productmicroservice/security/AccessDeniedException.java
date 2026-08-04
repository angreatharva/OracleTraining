package com.example.productmicroservice.security;

/** Raised when an authenticated caller lacks the role for an operation. Mapped to HTTP 403. */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
