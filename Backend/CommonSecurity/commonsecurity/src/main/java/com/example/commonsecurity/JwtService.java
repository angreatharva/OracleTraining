package com.example.commonsecurity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Issues and validates the HS256 tokens used across WealthTrack.
 *
 * <p>User Service is the only issuer of user tokens; the gateway and the five business
 * services are validators. Trading additionally issues short-lived {@code SERVICE} tokens
 * for Portfolio's internal trade commands.</p>
 *
 * <p><strong>Algorithm handling.</strong> The header is a fixed constant and is compared
 * byte-for-byte on verification rather than parsed for an {@code alg} value. A token is
 * therefore only accepted if it was produced by this exact HS256 profile - {@code alg:none}
 * and algorithm-confusion tokens fail before the signature is even considered.</p>
 *
 * <p>Thread-safe: the key material is immutable and a fresh {@link Mac} is created per call
 * ({@code Mac} instances are not thread-safe and must not be shared).</p>
 */
public class JwtService {

    // Declaration order matters: ENCODED_HEADER below is computed with URL_ENCODER, and
    // static initialisers run top to bottom.
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    /** {"alg":"HS256","typ":"JWT"} - fixed, never negotiated. */
    private static final String ENCODED_HEADER =
            encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /** Tolerance for clock drift between services when checking expiry. */
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JwtProperties properties;
    private final byte[] keyBytes;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
    }

    /** Issues a token for an end user, using the configured default expiry. */
    public String generate(AuthenticatedUser user) {
        return generate(user, properties.getExpiry());
    }

    /** Issues a token for an end user with an explicit lifetime. */
    public String generate(AuthenticatedUser user, Duration expiry) {
        Instant issuedAt = Instant.now();

        ObjectNode claims = MAPPER.createObjectNode();
        claims.put(JwtClaims.SUBJECT, String.valueOf(user.userId()));
        claims.put(JwtClaims.USER_ID, user.userId());
        claims.put(JwtClaims.ROLE_ID, user.roleId());
        claims.put(JwtClaims.ROLE, user.roleName());
        claims.put(JwtClaims.EMAIL, user.email());
        // Only set when present: a null claim and an absent claim should look the same.
        if (user.managerId() != null) {
            claims.put(JwtClaims.MANAGER_ID, user.managerId());
        }
        claims.put("iss", properties.getIssuer());
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", issuedAt.plus(expiry).getEpochSecond());

        String payload;
        try {
            payload = encode(MAPPER.writeValueAsBytes(claims));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialise JWT claims", exception);
        }

        String signingInput = ENCODED_HEADER + "." + payload;
        return signingInput + "." + encode(sign(signingInput));
    }

    /**
     * Issues a short-lived token representing a calling service rather than a person.
     * Used by Trading to invoke Portfolio's {@code /internal/**} endpoints, which are
     * closed to investor and manager tokens.
     *
     * @param serviceName recorded as the email claim for traceability
     */
    public String generateServiceToken(String serviceName, Duration expiry) {
        AuthenticatedUser serviceIdentity =
                new AuthenticatedUser(0L, 0L, JwtClaims.ROLE_SERVICE, null, serviceName);
        return generate(serviceIdentity, expiry);
    }

    /**
     * Parses and verifies a token.
     *
     * @return the identity it carries, or {@link Optional#empty()} if the token is
     *         malformed, tampered with, expired, or issued by someone else. Callers treat
     *         an empty result as "unauthenticated" - the reason is deliberately not exposed
     *         to clients.
     */
    public Optional<AuthenticatedUser> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        // Reject anything that is not our exact HS256 header before doing any other work.
        if (!ENCODED_HEADER.equals(parts[0])) {
            return Optional.empty();
        }

        byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
        byte[] presentedSignature;
        try {
            presentedSignature = URL_DECODER.decode(parts[2]);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        // Constant-time comparison: a byte-by-byte early exit leaks the signature.
        if (!MessageDigest.isEqual(expectedSignature, presentedSignature)) {
            return Optional.empty();
        }

        try {
            JsonNode claims = MAPPER.readTree(URL_DECODER.decode(parts[1]));

            if (!properties.getIssuer().equals(text(claims, "iss"))) {
                return Optional.empty();
            }

            JsonNode expiry = claims.get("exp");
            if (expiry == null || !expiry.canConvertToLong()) {
                return Optional.empty();
            }
            Instant expiresAt = Instant.ofEpochSecond(expiry.longValue());
            if (Instant.now().minus(CLOCK_SKEW).isAfter(expiresAt)) {
                return Optional.empty();
            }

            String roleName = text(claims, JwtClaims.ROLE);
            if (roleName == null || roleName.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new AuthenticatedUser(
                    number(claims, JwtClaims.USER_ID),
                    number(claims, JwtClaims.ROLE_ID),
                    roleName,
                    number(claims, JwtClaims.MANAGER_ID),
                    text(claims, JwtClaims.EMAIL)
            ));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    /** Reads the expiry instant of a token without validating it; for client display only. */
    public Optional<Instant> expiresAt(String token) {
        return parse(token).flatMap(user -> {
            String[] parts = token.split("\\.");
            try {
                JsonNode claims = MAPPER.readTree(URL_DECODER.decode(parts[1]));
                return Optional.of(Instant.ofEpochSecond(claims.get("exp").longValue()));
            } catch (Exception exception) {
                return Optional.empty();
            }
        });
    }

    /** Strips the {@code Bearer } prefix from an Authorization header value. */
    public static String stripBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtClaims.BEARER_PREFIX)) {
            return null;
        }
        String token = authorizationHeader.substring(JwtClaims.BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private byte[] sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(keyBytes, HMAC_ALGORITHM));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("HmacSHA256 is unavailable in this JVM", exception);
        }
    }

    private static String encode(byte[] value) {
        return URL_ENCODER.encodeToString(value);
    }

    private static String text(JsonNode claims, String name) {
        JsonNode node = claims.get(name);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static Long number(JsonNode claims, String name) {
        JsonNode node = claims.get(name);
        if (node == null || node.isNull() || !node.canConvertToLong()) {
            return null;
        }
        return node.longValue();
    }
}
