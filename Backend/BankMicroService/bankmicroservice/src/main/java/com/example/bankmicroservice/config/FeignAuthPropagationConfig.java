package com.example.bankmicroservice.config;

import com.example.commonsecurity.JwtClaims;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Copies the caller's bearer token onto outbound Feign calls.
 *
 * <p>Without this, Bank's call to User Service arrives unauthenticated and is rejected with
 * 401, breaking bank-account creation. Forwarding the end user's own token also means the
 * downstream service applies the same ownership rules to the nested call, rather than the
 * call being implicitly trusted.</p>
 *
 * <p>Only works on a request-scoped thread. Background or scheduled work has no incoming
 * request and will send no header - such callers need a service token instead.</p>
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
