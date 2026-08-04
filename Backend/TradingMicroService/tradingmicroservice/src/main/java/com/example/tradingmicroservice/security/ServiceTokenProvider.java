package com.example.tradingmicroservice.security;

import com.example.commonsecurity.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Mints the short-lived SERVICE tokens Trading uses for its outbound calls.
 *
 * <p>Trading is the only service that drives money movement and holding mutation, and both
 * Bank's debit/credit and Portfolio's {@code /internal/trades*} endpoints are closed to
 * end-user tokens. Forwarding the investor's own token would therefore fail - and would also
 * mean an investor's token was, transitively, able to move money.</p>
 *
 * <p>The token is cached and reissued shortly before expiry rather than minted per call;
 * signing is cheap but doing it once per outbound request is pure waste.</p>
 */
@Component
public class ServiceTokenProvider {

    /** Renew this long before the token actually expires, to cover clock drift and latency. */
    private static final Duration RENEWAL_MARGIN = Duration.ofSeconds(30);

    private final JwtService jwtService;
    private final String serviceName;
    private final Duration lifetime;

    private volatile String cachedToken;
    private volatile Instant cachedUntil = Instant.EPOCH;

    public ServiceTokenProvider(
            JwtService jwtService,
            @Value("${spring.application.name:TRADING-SERVICE}") String serviceName,
            @Value("${wealthtrack.jwt.service-token-seconds:120}") long lifetimeSeconds) {
        this.jwtService = jwtService;
        this.serviceName = serviceName;
        this.lifetime = Duration.ofSeconds(lifetimeSeconds);
    }

    /** @return a valid {@code Bearer}-ready SERVICE token. */
    public synchronized String currentToken() {
        if (cachedToken == null || Instant.now().isAfter(cachedUntil)) {
            cachedToken = jwtService.generateServiceToken(serviceName, lifetime);
            cachedUntil = Instant.now().plus(lifetime).minus(RENEWAL_MARGIN);
        }
        return cachedToken;
    }
}
