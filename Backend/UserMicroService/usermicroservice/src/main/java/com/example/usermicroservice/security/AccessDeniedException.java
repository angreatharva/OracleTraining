package com.example.usermicroservice.security;

/**
 * Raised when an authenticated caller is not allowed to touch the resource they asked for.
 * Mapped to HTTP 403 by {@code ApiExceptionHandler}.
 *
 * <p>Deliberately distinct from Spring Security's own {@code AccessDeniedException} so it
 * flows through this service's existing error-body format rather than Spring's.</p>
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
