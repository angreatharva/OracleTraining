package com.example.bankmicroservice.clients;

import com.example.bankmicroservice.clients.model.UserValidationResult;

/**
 * Boundary for the future User service integration.
 * Replace the temporary implementation with OpenFeign or RestClient once the User API is published.
 */
public interface UserServiceClient {

    UserValidationResult validateUser(Long userId);
}
