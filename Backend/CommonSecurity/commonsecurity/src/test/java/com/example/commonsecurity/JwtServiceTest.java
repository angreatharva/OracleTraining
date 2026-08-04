package com.example.commonsecurity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "wealthtrack-local-development-secret-key-value";
    private static final String ISSUER = "wealthtrack";

    private JwtService service() {
        return new JwtService(new JwtProperties(SECRET, ISSUER, Duration.ofHours(1)));
    }

    @Test
    void roundTripsAnInvestorIdentity() {
        JwtService service = service();
        AuthenticatedUser investor =
                new AuthenticatedUser(2L, 2L, JwtClaims.ROLE_INVESTOR, 1L, "investor@example.com");

        Optional<AuthenticatedUser> parsed = service.parse(service.generate(investor));

        assertTrue(parsed.isPresent());
        assertEquals(investor, parsed.get());
        assertTrue(parsed.get().isInvestor());
        assertFalse(parsed.get().isManager());
        assertEquals("ROLE_INVESTOR", parsed.get().authority());
    }

    @Test
    void managerWithoutAManagerHasNullManagerId() {
        JwtService service = service();
        AuthenticatedUser manager =
                new AuthenticatedUser(1L, 1L, JwtClaims.ROLE_MANAGER, null, "manager@example.com");

        AuthenticatedUser parsed = service.parse(service.generate(manager)).orElseThrow();

        assertNull(parsed.managerId());
        assertTrue(parsed.isManager());
    }

    @Test
    void rejectsATokenSignedWithADifferentSecret() {
        JwtService issuer = service();
        JwtService validator = new JwtService(
                new JwtProperties("a-completely-different-secret-of-sufficient-length", ISSUER, Duration.ofHours(1)));

        String token = issuer.generate(
                new AuthenticatedUser(2L, 2L, JwtClaims.ROLE_INVESTOR, 1L, "investor@example.com"));

        assertTrue(validator.parse(token).isEmpty());
    }

    @Test
    void rejectsATokenFromAnotherIssuer() {
        JwtService foreign = new JwtService(new JwtProperties(SECRET, "someone-else", Duration.ofHours(1)));
        String token = foreign.generate(
                new AuthenticatedUser(2L, 2L, JwtClaims.ROLE_INVESTOR, null, "investor@example.com"));

        assertTrue(service().parse(token).isEmpty());
    }

    @Test
    void rejectsAnExpiredToken() {
        JwtService service = service();
        // JWT expiry is second-granular, so back-date the token rather than sleeping.
        String token = service.generate(
                new AuthenticatedUser(2L, 2L, JwtClaims.ROLE_INVESTOR, null, "investor@example.com"),
                Duration.ofSeconds(-60));

        assertTrue(service.parse(token).isEmpty(), "an expired token must not authenticate");
    }

    @Test
    void rejectsGarbageAndMissingTokens() {
        JwtService service = service();
        assertTrue(service.parse(null).isEmpty());
        assertTrue(service.parse("").isEmpty());
        assertTrue(service.parse("not-a-jwt").isEmpty());
        assertTrue(service.parse("a.b.c").isEmpty());
    }

    @Test
    void serviceTokensAreDistinguishable() {
        JwtService service = service();
        AuthenticatedUser parsed = service
                .parse(service.generateServiceToken("TRADING-SERVICE", Duration.ofSeconds(30)))
                .orElseThrow();

        assertTrue(parsed.isService());
        assertFalse(parsed.isInvestor());
        assertFalse(parsed.isManager());
        assertEquals("TRADING-SERVICE", parsed.email());
    }

    @Test
    void stripsBearerPrefix() {
        assertEquals("abc", JwtService.stripBearer("Bearer abc"));
        assertNull(JwtService.stripBearer("abc"));
        assertNull(JwtService.stripBearer(null));
        assertNull(JwtService.stripBearer("Bearer "));
    }

    @Test
    void rejectsAnAlgNoneToken() {
        // The classic JWT bypass: swap the header to alg:none and drop the signature.
        String header = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"sub\":\"1\",\"userId\":1,\"roleId\":1,\"role\":\"MANAGER\","
                + "\"email\":\"a@b.c\",\"iss\":\"" + ISSUER + "\",\"exp\":" + far() + "}");

        assertTrue(service().parse(header + "." + payload + ".").isEmpty());
        assertTrue(service().parse(header + "." + payload + ".anything").isEmpty());
    }

    @Test
    void rejectsAPayloadTamperedToEscalateRole() {
        JwtService service = service();
        String token = service.generate(
                new AuthenticatedUser(2L, 2L, JwtClaims.ROLE_INVESTOR, 1L, "investor@example.com"));

        String[] parts = token.split("\\.");
        String escalated = base64Url("{\"sub\":\"2\",\"userId\":2,\"roleId\":1,\"role\":\"MANAGER\","
                + "\"email\":\"investor@example.com\",\"iss\":\"" + ISSUER + "\",\"exp\":" + far() + "}");

        // Same valid signature, different payload - must not authenticate.
        assertTrue(service.parse(parts[0] + "." + escalated + "." + parts[2]).isEmpty());
    }

    @Test
    void rejectsATokenWithTheSignatureRemoved() {
        JwtService service = service();
        String token = service.generate(
                new AuthenticatedUser(2L, 2L, JwtClaims.ROLE_INVESTOR, 1L, "investor@example.com"));
        String[] parts = token.split("\\.");

        assertTrue(service.parse(parts[0] + "." + parts[1] + ".").isEmpty());
        assertTrue(service.parse(parts[0] + "." + parts[1]).isEmpty());
    }

    private static String base64Url(String json) {
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static long far() {
        return java.time.Instant.now().plus(Duration.ofHours(1)).getEpochSecond();
    }

    @Test
    void rejectsAWeakSecretAtConstruction() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties("too-short", ISSUER, Duration.ofHours(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties(null, ISSUER, Duration.ofHours(1)));
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties(SECRET, ISSUER, Duration.ZERO));
    }
}
