package com.example.usermicroservice.dto.request;

import com.example.usermicroservice.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Full replacement of a user's profile fields.
 *
 * <p>Split out from {@code CreateUserRequest} because that one now carries a password:
 * reusing it for PUT would force the caller to resend a password on every edit, and a
 * password change belongs on {@code POST /api/auth/change-password} instead.</p>
 */
public record UpdateUserRequest(
        @NotNull @Positive Long roleId,
        @Positive Long managerId,
        @NotBlank @Email String email,
        @NotBlank String fullName,
        String phone,
        UserStatus status
) {
}
