package com.example.usermicroservice.security;

import com.example.commonsecurity.AuthenticatedUser;
import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Central place for "may this caller touch this user?".
 *
 * <p>The rule is the same everywhere in the system:</p>
 * <ul>
 *   <li>a SERVICE token may do anything (internal calls between microservices);</li>
 *   <li>anyone may act on themselves;</li>
 *   <li>a MANAGER may additionally act on their own direct reports;</li>
 *   <li>everything else is denied.</li>
 * </ul>
 *
 * <p>Note the manager check reads the <em>target user's</em> managerId from the database
 * rather than trusting anything in the token, so a stale token cannot widen access after a
 * user is reassigned to a different manager.</p>
 */
@Component
public class AuthorizationHelper {

    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public AuthorizationHelper(CurrentUser currentUser, UserRepository userRepository) {
        this.currentUser = currentUser;
        this.userRepository = userRepository;
    }

    /** Throws {@link AccessDeniedException} unless the caller may access {@code targetUserId}. */
    public void assertCanAccessUser(Long targetUserId) {
        if (!canAccessUser(targetUserId)) {
            throw new AccessDeniedException("Not permitted to access user " + targetUserId);
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
        return userRepository.findById(targetUserId)
                .map(User::getManager)
                .map(manager -> manager.getUserId().equals(caller.userId()))
                .orElse(false);
    }

    /** Throws unless the caller holds the MANAGER role. */
    public void assertManager() {
        AuthenticatedUser caller = currentUser.require();
        if (!caller.isManager() && !caller.isService()) {
            throw new AccessDeniedException("This operation requires the MANAGER role");
        }
    }

    public AuthenticatedUser caller() {
        return currentUser.require();
    }
}
