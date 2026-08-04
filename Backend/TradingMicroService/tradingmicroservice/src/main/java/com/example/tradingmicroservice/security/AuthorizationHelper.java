package com.example.tradingmicroservice.security;

import com.example.commonsecurity.AuthenticatedUser;
import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.UserServiceClient;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import feign.FeignException;
import org.springframework.stereotype.Component;

/**
 * Ownership rules for Trading.
 *
 * <p>This is the awkward one. A {@code trade_transaction} row stores a
 * {@code portfolio_account_id} and no user id at all, so Trading cannot answer "whose trade
 * is this?" from its own tables - it has to ask Portfolio. Establishing a manager
 * relationship then needs a second hop to User Service.</p>
 *
 * <p>Both hops use Trading's SERVICE token (see {@link ServiceTokenProvider}), so the lookup
 * cannot itself be blocked by the very rule it is trying to evaluate.</p>
 */
@Component
public class AuthorizationHelper {

    private final CurrentUser currentUser;
    private final PortfolioServiceClient portfolioServiceClient;
    private final UserServiceClient userServiceClient;

    public AuthorizationHelper(CurrentUser currentUser,
                               PortfolioServiceClient portfolioServiceClient,
                               UserServiceClient userServiceClient) {
        this.currentUser = currentUser;
        this.portfolioServiceClient = portfolioServiceClient;
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

    /** Resolves the owner of a portfolio account via Portfolio Service, then applies the rule. */
    public void assertCanAccessPortfolioAccount(Long portfolioAccountId) {
        if (currentUser.require().isService()) {
            return;
        }
        assertCanAccessUser(resolveOwner(portfolioAccountId));
    }

    private Long resolveOwner(Long portfolioAccountId) {
        if (portfolioAccountId == null) {
            return null;
        }
        try {
            PortfolioAccountSummary account = portfolioServiceClient.getAccount(portfolioAccountId);
            return account == null ? null : account.userId();
        } catch (FeignException exception) {
            // Cannot establish ownership -> deny. Failing closed is the only safe choice for
            // an authorization check that depends on a remote call.
            return null;
        }
    }

    /**
     * Narrows a trade/statement list query to what the caller may see.
     *
     * <p>An investor is pinned to their own portfolio account regardless of what they asked
     * for; without this, omitting the filter would return every trade in the system.</p>
     */
    public Long restrictPortfolioAccountFilter(Long requestedPortfolioAccountId) {
        AuthenticatedUser caller = currentUser.require();

        if (caller.isService()) {
            return requestedPortfolioAccountId;
        }
        if (requestedPortfolioAccountId != null) {
            assertCanAccessPortfolioAccount(requestedPortfolioAccountId);
            return requestedPortfolioAccountId;
        }
        if (caller.isManager()) {
            throw new AccessDeniedException(
                    "portfolioAccountId is required: specify whose trades to list");
        }
        // Investor with no filter: resolve their own account and pin to it.
        try {
            PortfolioAccountSummary own = portfolioServiceClient.getAccountByUser(caller.userId());
            if (own == null || own.portfolioAccountId() == null) {
                throw new AccessDeniedException("No portfolio account exists for this user");
            }
            return own.portfolioAccountId();
        } catch (FeignException exception) {
            throw new AccessDeniedException("Unable to resolve the caller's portfolio account");
        }
    }

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
