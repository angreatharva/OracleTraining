package com.example.usermicroservice.config;

import com.example.commonsecurity.JwtProperties;
import com.example.commonsecurity.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Supplies the JWT beans.
 *
 * <p>Kept separate from {@code SecurityConfig} on purpose: {@code JwtAuthenticationFilter}
 * depends on {@link JwtService}, and {@code SecurityConfig} depends on the filter. Declaring
 * the JwtService bean inside SecurityConfig makes those two beans a cycle, which Spring Boot
 * rejects at startup.</p>
 */
@Configuration
public class JwtConfig {

    /**
     * No default value. A missing *_JWT_SECRET must stop startup rather than let the service
     * come up with an empty signing key.
     */
    @Bean
    public JwtProperties jwtProperties(
            @Value("${wealthtrack.jwt.secret}") String secret,
            @Value("${wealthtrack.jwt.issuer:wealthtrack}") String issuer,
            @Value("${wealthtrack.jwt.expiry-seconds:3600}") long expirySeconds) {
        return new JwtProperties(secret, issuer, Duration.ofSeconds(expirySeconds));
    }

    @Bean
    public JwtService jwtService(JwtProperties jwtProperties) {
        return new JwtService(jwtProperties);
    }
}
