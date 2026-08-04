package com.example.apigateway.config;

import com.example.commonsecurity.AuthenticatedUser;
import com.example.commonsecurity.JwtClaims;
import com.example.commonsecurity.JwtService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * The gateway's edge authentication check.
 *
 * <p>Runs ahead of routing and the Resilience4j circuit breakers, so an unauthenticated
 * request is rejected before any downstream call is attempted. The five business services
 * validate the token again independently - this filter is the outer layer, not the only
 * one, because ports 8082-8086 are reachable without passing through here at all.</p>
 *
 * <p>It also does something the services cannot: block the {@code /internal/**} command
 * endpoints from the outside entirely. Those are meant for service-to-service calls, and
 * routing them from the public edge serves no purpose.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class JwtAuthenticationWebFilter implements WebFilter {

    /** Reachable without a token: login, the circuit-breaker fallbacks, and docs. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/user/api/auth/login",
            "/userFallback",
            "/bankFallback",
            "/portfolioFallback",
            "/tradingFallback",
            "/productFallback",
            "/actuator/health",
            "/swagger-ui",
            "/v3/api-docs"
    );

    /**
     * Service-to-service command endpoints. They are deliberately unreachable through the
     * gateway; Trading calls them directly by Eureka service name with a SERVICE token.
     */
    private static final List<String> BLOCKED_PATHS = List.of(
            "/portfolio/api/portfolios/internal",
            "/trading/api/portfolio-statements/internal"
    );

    private final JwtService jwtService;

    public JwtAuthenticationWebFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // CORS preflight carries no Authorization header by design; rejecting it would make
        // every cross-origin call fail before the real request is ever sent.
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        if (isBlocked(path)) {
            return deny(exchange, HttpStatus.FORBIDDEN);
        }

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String token = JwtService.stripBearer(request.getHeaders().getFirst(JwtClaims.AUTHORIZATION_HEADER));
        Optional<AuthenticatedUser> user = jwtService.parse(token);

        if (user.isEmpty()) {
            return deny(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Forward the resolved identity for downstream logging and tracing. These headers are
        // informational only - each service re-derives identity from the token itself, so a
        // spoofed header cannot grant access. Any client-supplied value is overwritten here.
        AuthenticatedUser authenticated = user.get();
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder
                        .header(JwtClaims.HEADER_USER_ID, String.valueOf(authenticated.userId()))
                        .header(JwtClaims.HEADER_USER_ROLE, authenticated.roleName()))
                .build();

        return chain.filter(mutated);
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isBlocked(String path) {
        return BLOCKED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
