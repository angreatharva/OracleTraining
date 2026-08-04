package com.example.bankmicroservice.exceptions;

public class UserValidationException extends RuntimeException {

    public UserValidationException(Long userId, String reason) {
        super("User " + userId + " cannot be used: " + reason);
    }
}
