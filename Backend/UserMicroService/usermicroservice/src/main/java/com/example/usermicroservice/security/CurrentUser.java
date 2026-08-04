package com.example.usermicroservice.security;

import com.example.commonsecurity.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the authenticated caller out of the security context.
 *
 * <p>A bean rather than a static helper so it can be swapped in tests.</p>
 */
@Component
public class CurrentUser {

    /**
     * @return the authenticated caller
     * @throws IllegalStateException if there is none - which should be unreachable, because
     *         every non-public endpoint is already gated by the security filter chain
     */
    public AuthenticatedUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("No authenticated user in the security context");
        }
        return user;
    }
}
