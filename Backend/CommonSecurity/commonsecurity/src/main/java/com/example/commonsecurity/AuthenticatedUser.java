package com.example.commonsecurity;

/**
 * The identity carried by a validated JWT.
 *
 * <p>This is what every service's authorization checks operate on. {@code managerId} is
 * included in the token so that an investor's "who is my manager" relationship does not
 * require a User Service round trip on every request.</p>
 *
 * @param userId    the authenticated user's id (the token subject)
 * @param roleId    numeric role id (1 = MANAGER, 2 = INVESTOR as seeded in the schema)
 * @param roleName  role name, one of {@link JwtClaims#ROLE_MANAGER},
 *                  {@link JwtClaims#ROLE_INVESTOR} or {@link JwtClaims#ROLE_SERVICE}
 * @param managerId id of this user's manager, or {@code null} for a top-level user
 * @param email     the user's email address
 */
public record AuthenticatedUser(
        Long userId,
        Long roleId,
        String roleName,
        Long managerId,
        String email
) {

    public boolean isManager() {
        return JwtClaims.ROLE_MANAGER.equals(roleName);
    }

    public boolean isInvestor() {
        return JwtClaims.ROLE_INVESTOR.equals(roleName);
    }

    /** True for internal service-to-service tokens, which bypass ownership rules. */
    public boolean isService() {
        return JwtClaims.ROLE_SERVICE.equals(roleName);
    }

    /** The Spring Security authority name for this user's role. */
    public String authority() {
        return JwtClaims.AUTHORITY_PREFIX + roleName;
    }
}
