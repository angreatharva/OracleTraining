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
     * Resolves the manager of a user, for the "a MANAGER may act on their direct reports"
     * authorization rule.
     *
     * @return the manager's user id, or {@code null} if the user has no manager or cannot
     *         be read. Returning null rather than throwing keeps a User Service hiccup from
     *         turning an authorization check into a 500 - the caller simply gets denied.
     */
    default Long findManagerId(Long userId) {
        try {
            UserResponse user = getUserById(userId);
            return user == null ? null : user.managerId();
        } catch (FeignException exception) {
            return null;
        }
    }

    /**
     * Minimal projection of User Service's response needed by Bank Service. Unknown JSON
     * fields are ignored by Boot's default ObjectMapper, so this stays a partial view.
     */
    record UserResponse(Long userId, Long managerId, String status) {
    }
}
