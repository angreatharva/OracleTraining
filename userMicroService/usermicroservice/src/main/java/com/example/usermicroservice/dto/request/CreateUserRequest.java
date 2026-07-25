package com.example.usermicroservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.example.usermicroservice.enums.UserStatus;

public record CreateUserRequest(
        @NotNull @Positive Long roleId,
        @Positive Long managerId,
        @NotBlank String passwordHash,
        @NotBlank @Email String email,
        @NotBlank String fullName,
        String phone,
        UserStatus status
) {
}
