package com.example.usermicroservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.example.usermicroservice.enums.UserStatus;

/**
 * Creates a user.
 *
 * <p>The field is {@code password}, not {@code passwordHash}: the plaintext arrives here and
 * is BCrypt-hashed by the service. Previously the client sent a value called
 * {@code passwordHash} which was stored verbatim, meaning whatever the caller typed became
 * the "hash" and no password could ever be verified.</p>
 */
public record CreateUserRequest(
        @NotNull @Positive Long roleId,
        @Positive Long managerId,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String password,
        @NotBlank @Email String email,
        @NotBlank String fullName,
        String phone,
        UserStatus status
) {
}
