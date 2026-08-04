package com.example.bankmicroservice.security;

import com.example.bankmicroservice.clients.UserServiceClient;
import com.example.commonsecurity.AuthenticatedUser;
import org.springframework.stereotype.Component;

/**
 * "May this caller touch data belonging to user X?"
 *
 * <p>Bank owns {@code bank_account} and {@code kyc_document}, both of which carry a
 * {@code user_id}, so ownership is resolved from the row itself. Establishing the manager
 * relationship needs User Service, because Bank does not store it - that call is made only
 * for MANAGER callers, so the common investor path stays purely local.</p>
 */
@Component
public class AuthorizationHelper {

    private final CurrentUser currentUser;
    private final UserServiceClient userServiceClient;

    public AuthorizationHelper(CurrentUser currentUser, UserServiceClient userServiceClient) {
        this.currentUser = currentUser;
        this.userServiceClient = userServiceClient;
    }

    public void assertCanAccessUser(Long targetUserId) {
        if (!canAccessUser(targetUserId)) {
            throw new AccessDeniedException("Not permitted to access data for user " + targetUserId);
        }
    }

    public boolean canAccessUser(Long targetUserId) {
        AuthenticatedUser caller = currentUser.require();

        if (caller.isService()) {
            return true;
        }
        if (targetUserId == null) {
            return false;
        }
        if (targetUserId.equals(caller.userId())) {
            return true;
        }
        if (!caller.isManager()) {
            return false;
        }
        return caller.userId().equals(userServiceClient.findManagerId(targetUserId));
    }

    /** Throws unless the caller holds the MANAGER role (or is an internal service call). */
    public void assertManager() {
        AuthenticatedUser caller = currentUser.require();
        if (!caller.isManager() && !caller.isService()) {
            throw new AccessDeniedException("This operation requires the MANAGER role");
        }
    }

    /**
     * Throws unless the caller presented a SERVICE token.
     *
     * <p>Guards debit and credit. Those endpoints move money and are only ever meant to be
     * driven by the Trading saga; if an end user's token could call them, an investor could
     * credit their own account without a matching trade.</p>
     */
    public void assertServiceCall() {
        if (!currentUser.require().isService()) {
            throw new AccessDeniedException(
                    "This endpoint is only callable by another WealthTrack service");
        }
    }

    /**
     * Restricts a list query to what the caller may see.
     *
     * <p>An investor may only ever filter by their own id, so an absent or foreign
     * {@code userId} is replaced by their own rather than returning the whole table. This is
     * the one place where silently narrowing a query is right: the alternative is leaking
     * every bank account in the system to any authenticated user.</p>
     */
    public Long restrictUserFilter(Long requestedUserId) {
        AuthenticatedUser caller = currentUser.require();
        if (caller.isService()) {
            return requestedUserId;
        }
        if (caller.isManager()) {
            // A manager must still name whose data they want, and it must be their report.
            if (requestedUserId == null) {
                throw new AccessDeniedException("userId is required: specify which user's records to list");
            }
            assertCanAccessUser(requestedUserId);
            return requestedUserId;
        }
        return caller.userId();
    }

    public AuthenticatedUser caller() {
        return currentUser.require();
    }
}
