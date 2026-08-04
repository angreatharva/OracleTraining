package com.example.productmicroservice.security;

import com.example.commonsecurity.AuthenticatedUser;
import org.springframework.stereotype.Component;

/**
 * The product catalogue is shared reference data - there is no per-user ownership here, so
 * this service only needs a role check.
 *
 * <p>Every authenticated user may read the catalogue (an investor cannot trade a product
 * they cannot see). Only a MANAGER may change it, including prices: {@code currentPrice} is
 * the value Trading executes at, so write access to this catalogue is effectively write
 * access to every trade's execution price.</p>
 */
@Component
public class AuthorizationHelper {

    private final CurrentUser currentUser;

    public AuthorizationHelper(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void assertManager() {
        AuthenticatedUser caller = currentUser.require();
        if (!caller.isManager() && !caller.isService()) {
            throw new AccessDeniedException("This operation requires the MANAGER role");
        }
    }
}
