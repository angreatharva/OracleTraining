package com.example.usermicroservice.dto.response;

import java.time.LocalDateTime;
import com.example.usermicroservice.enums.UserStatus;

public record UserResponse(
        Long userId,
        Long roleId,
        Long managerId,
        String email,
        String fullName,
        String phone,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
