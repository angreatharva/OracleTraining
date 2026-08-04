package com.example.portfoliomicroservice.config;

import com.example.commonsecurity.JwtClaims;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the caller's bearer token onto Portfolio's outbound calls to User and Product.
 * Without it those calls arrive unauthenticated and are rejected with 401.
 */
@Configuration
public class FeignAuthPropagationConfig {

    @Bean
    public RequestInterceptor authorizationPropagationInterceptor() {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return;
            }
            String authorization = attributes.getRequest().getHeader(JwtClaims.AUTHORIZATION_HEADER);
            if (authorization != null && !authorization.isBlank()
                    && !template.headers().containsKey(JwtClaims.AUTHORIZATION_HEADER)) {
                template.header(JwtClaims.AUTHORIZATION_HEADER, authorization);
            }
        };
    }
}
