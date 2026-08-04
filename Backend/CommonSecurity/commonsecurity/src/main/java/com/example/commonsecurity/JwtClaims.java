package com.example.commonsecurity;

/**
 * Claim names shared by the token issuer (User Service), the API Gateway, and every
 * business service that validates a token.
 *
 * <p>These are constants rather than inline strings so the issuer and the validators
 * cannot drift apart silently - a renamed claim becomes a compile error instead of a
 * runtime authorization hole.</p>
 */
public final class JwtClaims {

    /** Standard subject claim; holds the numeric user id as a string. */
    public static final String SUBJECT = "sub";

    public static final String USER_ID = "userId";
    public static final String ROLE_ID = "roleId";
    public static final String ROLE = "role";
    public static final String MANAGER_ID = "managerId";
    public static final String EMAIL = "email";

    /** Role name of an investor; matches role_id 2 seeded in the schema. */
    public static final String ROLE_INVESTOR = "INVESTOR";

    /** Role name of a manager; matches role_id 1 seeded in the schema. */
    public static final String ROLE_MANAGER = "MANAGER";

    /**
     * Pseudo-role carried only by short-lived service tokens. Used to gate the Portfolio
     * {@code /internal/**} trade commands so an end user's token cannot invoke them.
     */
    public static final String ROLE_SERVICE = "SERVICE";

    /** Spring Security expects authorities to be prefixed; kept here so all services agree. */
    public static final String AUTHORITY_PREFIX = "ROLE_";

    /** Header used to pass the bearer token. */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PREFIX = "Bearer ";

    /** Identity headers the gateway forwards downstream for logging/tracing. */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    private JwtClaims() {
    }
}
