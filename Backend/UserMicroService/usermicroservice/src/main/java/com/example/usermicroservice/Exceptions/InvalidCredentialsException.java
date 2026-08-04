package com.example.usermicroservice.exceptions;

/**
 * Raised when a login attempt fails, or when a password change supplies the wrong current
 * password. Mapped to HTTP 401.
 *
 * <p>The message is deliberately identical for "no such email", "wrong password" and
 * "account not active": telling a caller which of the three happened turns the login
 * endpoint into an account-enumeration oracle.</p>
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
