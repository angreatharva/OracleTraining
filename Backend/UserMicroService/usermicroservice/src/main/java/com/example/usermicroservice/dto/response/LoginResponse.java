package com.example.usermicroservice.dto.response;

import java.time.LocalDateTime;

/**
 * Issued on successful login.
 *
 * @param token     the bearer token to send as {@code Authorization: Bearer <token>}
 * @param expiresAt when the token stops being accepted
 * @param roleName  MANAGER or INVESTOR; the frontend routes on this
 * @param user      the authenticated user's profile, so the client need not call /me
 */
public record LoginResponse(
        String token,
        LocalDateTime expiresAt,
        String roleName,
        UserResponse user
) {
}
