package com.example.commonsecurity;

import java.time.Duration;

/**
 * JWT settings, supplied by each service from its own {@code .env} using its service
 * prefix (USER_JWT_SECRET, BANK_JWT_SECRET, ...). A plain POJO rather than a
 * {@code @ConfigurationProperties} bean so this module stays free of Spring.
 *
 * <p>The secret must be identical across every service and the gateway: tokens are signed
 * with HS256, so the issuer and all validators share one key.</p>
 */
public class JwtProperties {

    /**
     * HS256 requires a key of at least 256 bits. A shorter secret makes jjwt throw at
     * startup rather than silently weakening the signature, which is the behaviour we want.
     */
    public static final int MINIMUM_SECRET_LENGTH = 32;

    private final String secret;
    private final String issuer;
    private final Duration expiry;

    public JwtProperties(String secret, String issuer, Duration expiry) {
        if (secret == null || secret.trim().length() < MINIMUM_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least " + MINIMUM_SECRET_LENGTH
                            + " characters. Set the *_JWT_SECRET variable in this service's .env file.");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer must not be blank");
        }
        if (expiry == null || expiry.isZero() || expiry.isNegative()) {
            throw new IllegalArgumentException("JWT expiry must be a positive duration");
        }
        this.secret = secret;
        this.issuer = issuer;
        this.expiry = expiry;
    }

    public String getSecret() {
        return secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public Duration getExpiry() {
        return expiry;
    }
}
