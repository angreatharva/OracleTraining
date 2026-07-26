package com.example.portfoliomicroservice.clients;

import com.example.portfoliomicroservice.clients.model.UserResponse;
import com.example.portfoliomicroservice.exceptions.BusinessRuleException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long userId);

    default void validateUser(Long userId) {
        try {
            UserResponse response = getUserById(userId);
            if (response == null || response.userId() == null) {
                throw new BusinessRuleException("User " + userId + " is not valid or unavailable");
            }
            if (response.status() != null && !"ACTIVE".equalsIgnoreCase(response.status())) {
                throw new BusinessRuleException("User " + userId + " is not valid or unavailable");
            }
        } catch (FeignException.NotFound ex) {
            throw new BusinessRuleException("User " + userId + " is not valid or unavailable");
        } catch (FeignException ex) {
            throw new BusinessRuleException("User Service is currently unavailable");
        }
    }
}
