package com.example.portfoliomicroservice.clients.model;

/**
 * Partial view of User Service's response. {@code managerId} backs the "a MANAGER may act on
 * their direct reports" rule; unknown JSON fields are ignored by Boot's default ObjectMapper.
 */
public record UserResponse(Long userId, Long managerId, String status) {
}
