package com.example.portfoliomicroservice.security;

import com.example.commonsecurity.AuthenticatedUser;
import com.example.portfoliomicroservice.clients.UserServiceClient;
import com.example.portfoliomicroservice.entities.PortfolioAccount;
import com.example.portfoliomicroservice.entities.PortfolioHolding;
import com.example.portfoliomicroservice.exceptions.ResourceNotFoundException;
import com.example.portfoliomicroservice.repositories.PortfolioAccountRepository;
import com.example.portfoliomicroservice.repositories.PortfolioHoldingRepository;
import org.springframework.stereotype.Component;

/**
 * Ownership rules for Portfolio.
 *
 * <p>Portfolio owns {@code portfolio_account} (which carries {@code user_id}) and
 * {@code portfolio_holding} (which reaches a user only through its account). So an account
 * check is a single local read, and a holding check is a local read plus a hop to its
 * account - no remote call on the investor path.</p>
 */
@Component
public class AuthorizationHelper {

    private final CurrentUser currentUser;
    private final UserServiceClient userServiceClient;
    private final PortfolioAccountRepository accountRepository;
    private final PortfolioHoldingRepository holdingRepository;

    public AuthorizationHelper(CurrentUser currentUser,
                               UserServiceClient userServiceClient,
                               PortfolioAccountRepository accountRepository,
                               PortfolioHoldingRepository holdingRepository) {
        this.currentUser = currentUser;
        this.userServiceClient = userServiceClient;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
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

    /** Resolves the owner of a portfolio account and checks the caller may act for them. */
    public void assertCanAccessAccount(Long portfolioAccountId) {
        if (currentUser.require().isService()) {
            return;
        }
        PortfolioAccount account = accountRepository.findById(portfolioAccountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Portfolio account " + portfolioAccountId + " was not found"));
        assertCanAccessUser(account.getUserId());
    }

    /** Same, reached through the holding's owning account. */
    public void assertCanAccessHolding(Long holdingId) {
        if (currentUser.require().isService()) {
            return;
        }
        PortfolioHolding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holding " + holdingId + " was not found"));
        assertCanAccessUser(holding.getPortfolioAccount().getUserId());
    }

    public void assertManager() {
        AuthenticatedUser caller = currentUser.require();
        if (!caller.isManager() && !caller.isService()) {
            throw new AccessDeniedException("This operation requires the MANAGER role");
        }
    }

    /**
     * Guards the {@code /internal/**} trade commands.
     *
     * <p>These apply holding changes for an already-funded trade and are not idempotent, so
     * they must only ever be driven by Trading. An investor or manager token is rejected
     * even though those endpoints are reachable through the gateway.</p>
     */
    public void assertServiceCall() {
        if (!currentUser.require().isService()) {
            throw new AccessDeniedException(
                    "This endpoint is only callable by another WealthTrack service");
        }
    }

    public AuthenticatedUser caller() {
        return currentUser.require();
    }
}
