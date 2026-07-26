package com.example.bankmicroservice.clients;

import com.example.bankmicroservice.clients.model.UserValidationResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Temporary implementation while the User service has no API.
 * It allows local development without inventing a shared User entity/database relationship.
 */
@Component
@ConditionalOnProperty(
        prefix = "clients.user",
        name = "validation-enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoOpUserServiceClient implements UserServiceClient {

    @Override
    public UserValidationResult validateUser(Long userId) {
        return new UserValidationResult(
                userId,
                true,
                true,
                "User validation is temporarily bypassed"
        );
    }
}
