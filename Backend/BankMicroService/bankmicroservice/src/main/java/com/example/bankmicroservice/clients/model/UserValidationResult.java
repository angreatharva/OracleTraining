package com.example.bankmicroservice.clients.model;

public record UserValidationResult(
        Long userId,
        boolean exists,
        boolean active,
        String message
) {
    public boolean usable() {
        return exists && active;
    }
}
