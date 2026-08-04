package com.example.usermicroservice.exceptions;

public class UserDeletionBlockedException extends RuntimeException {

    public UserDeletionBlockedException(Long userId) {
        super("User " + userId + " cannot be deleted while Bank accounts or KYC documents exist");
    }

    public UserDeletionBlockedException(Long userId, String reason, Throwable cause) {
        super("User " + userId + " cannot be deleted: " + reason, cause);
    }
}
