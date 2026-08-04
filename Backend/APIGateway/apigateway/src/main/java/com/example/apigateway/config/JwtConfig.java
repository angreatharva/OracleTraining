package com.example.apigateway.config;

import com.example.commonsecurity.JwtProperties;
import com.example.commonsecurity.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * JWT beans for the gateway. The gateway never issues tokens - it only verifies them - but
 * it needs the same secret and issuer as User Service for the signature to check out.
 */
@Configuration
public class JwtConfig {

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
