package com.example.tradingmicroservice.clients;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User API contract, added so Trading can answer "does this investor report to the manager
 * who is asking?". Trading stores no user id of its own - a trade only knows its portfolio
 * account - so the reporting line cannot be resolved locally.
 */
@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);

    /**
     * @return the manager's user id, or {@code null} if there is none or User Service could
     *         not answer. Null means "deny", the safe direction for an authorization check.
     */
    default Long findManagerId(Long userId) {
        try {
            UserResponse response = getUserById(userId);
            return response == null ? null : response.managerId();
        } catch (FeignException exception) {
            return null;
        }
    }

    /** Partial view; unknown JSON fields are ignored by Boot's default ObjectMapper. */
    record UserResponse(Long userId, Long managerId, String status) {
    }
}
