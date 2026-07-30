package com.example.bankmicroservice.clients;

import com.example.bankmicroservice.clients.model.UserValidationResult;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User API contract resolved through Eureka by service name.
 */
@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);

    default UserValidationResult validateUser(Long userId) {
        try {
            UserResponse user = getUserById(userId);
            boolean exists = user != null && user.userId() != null;
            boolean active = exists && "ACTIVE".equalsIgnoreCase(user.status());
            String message = !exists ? "User does not exist"
                    : active ? "User is active" : "User is not ACTIVE";
            return new UserValidationResult(userId, exists, active, message);
        } catch (FeignException.NotFound exception) {
            return new UserValidationResult(userId, false, false, "User does not exist");
        } catch (FeignException exception) {
            throw new IllegalStateException("User Service is currently unavailable", exception);
        }
    }

    /**
     * Minimal projection of User Service's response needed by Bank Service.
     */
    record UserResponse(Long userId, String status) {
    }
}
