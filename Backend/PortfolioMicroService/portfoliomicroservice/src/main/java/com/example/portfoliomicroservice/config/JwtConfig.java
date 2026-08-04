package com.example.portfoliomicroservice.config;

import com.example.commonsecurity.JwtProperties;
import com.example.commonsecurity.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * JWT beans, kept out of {@code SecurityConfig} to avoid the bean cycle
 * SecurityConfig -> JwtAuthenticationFilter -> JwtService.
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
