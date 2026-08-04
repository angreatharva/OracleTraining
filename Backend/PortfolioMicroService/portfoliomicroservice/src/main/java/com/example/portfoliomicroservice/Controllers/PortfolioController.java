package com.example.portfoliomicroservice.controllers;

import com.example.portfoliomicroservice.dto.request.CreateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.dto.request.UpdateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.UpdatePortfolioStatusRequest;
import com.example.portfoliomicroservice.dto.request.ApplyTradeRequest;
import com.example.portfoliomicroservice.dto.response.PortfolioAccountResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioHoldingResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioSummaryResponse;
import com.example.portfoliomicroservice.enums.HoldingStatus;
import com.example.portfoliomicroservice.security.AuthorizationHelper;
import com.example.portfoliomicroservice.services.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Authorization notes:
 * <ul>
 *   <li>account and holding reads resolve the owning user and apply the standard
 *       self-or-my-manager rule;</li>
 *   <li>opening a portfolio account and changing its status are MANAGER-only (onboarding
 *       and administrative actions);</li>
 *   <li>the two {@code /internal/trades*} commands are SERVICE-only - they mutate holdings
 *       for an already-funded trade and are not idempotent, so an end-user token must never
 *       reach them even though the gateway routes the path.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController implements IPortfolioController {

    private final PortfolioService portfolioService;
    private final AuthorizationHelper authorization;

    public PortfolioController(PortfolioService portfolioService, AuthorizationHelper authorization) {
        this.portfolioService = portfolioService;
        this.authorization = authorization;
    }

    @PostMapping
    @Override
    public ResponseEntity<PortfolioAccountResponse> createAccount(@Valid @RequestBody CreatePortfolioAccountRequest request) {
        authorization.assertManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.createAccount(request));
    }

    /** Listing every portfolio in the system is a manager-only view. */
    @GetMapping
    @Override
    public List<PortfolioAccountResponse> getAccounts() {
        authorization.assertManager();
        return portfolioService.getAccounts();
    }

    @GetMapping("/{portfolioAccountId}")
    @Override
    public PortfolioAccountResponse getAccount(@PathVariable Long portfolioAccountId) {
        authorization.assertCanAccessAccount(portfolioAccountId);
        return portfolioService.getAccount(portfolioAccountId);
    }

    @GetMapping("/by-user/{userId}")
    @Override
    public PortfolioAccountResponse getAccountByUser(@PathVariable Long userId) {
        authorization.assertCanAccessUser(userId);
        return portfolioService.getAccountByUser(userId);
    }

    @PatchMapping("/{portfolioAccountId}/status")
    @Override
    public PortfolioAccountResponse updateStatus(@PathVariable Long portfolioAccountId,
                                                 @Valid @RequestBody UpdatePortfolioStatusRequest request) {
        // Suspending or closing an account is an administrative act, not a self-service one.
        authorization.assertManager();
        return portfolioService.updateStatus(portfolioAccountId, request);
    }

    @PostMapping("/{portfolioAccountId}/holdings")
    @Override
    public ResponseEntity<PortfolioHoldingResponse> addHolding(@PathVariable Long portfolioAccountId,
                                                               @Valid @RequestBody CreateHoldingRequest request) {
        // Investors open their own holdings: the trade screen does this before a first BUY.
        authorization.assertCanAccessAccount(portfolioAccountId);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.addHolding(portfolioAccountId, request));
    }

    @GetMapping("/{portfolioAccountId}/holdings")
    @Override
    public List<PortfolioHoldingResponse> getHoldings(@PathVariable Long portfolioAccountId,
                                                      @RequestParam(required = false) HoldingStatus status) {
        authorization.assertCanAccessAccount(portfolioAccountId);
        return portfolioService.getHoldings(portfolioAccountId, status);
    }

    @GetMapping("/holdings/{holdingId}")
    @Override
    public PortfolioHoldingResponse getHolding(@PathVariable Long holdingId) {
        authorization.assertCanAccessHolding(holdingId);
        return portfolioService.getHolding(holdingId);
    }

    @PatchMapping("/holdings/{holdingId}")
    @Override
    public PortfolioHoldingResponse updateHolding(@PathVariable Long holdingId,
                                                  @Valid @RequestBody UpdateHoldingRequest request) {
        // Manager-only: quantity and averageCost are trade-derived, so hand-editing them
        // would let an investor rewrite their own cost basis.
        authorization.assertManager();
        return portfolioService.updateHolding(holdingId, request);
    }

    /** Internal endpoint called by Trading after a bank operation succeeds. */
    @PostMapping("/internal/trades")
    @Override
    public ResponseEntity<Void> applyCompletedTrade(@Valid @RequestBody ApplyTradeRequest request) {
        authorization.assertServiceCall();
        portfolioService.applyCompletedTrade(request);
        return ResponseEntity.noContent().build();
    }

    /** Internal pre-check used by Trading before it moves money. */
    @PostMapping("/internal/trades/validate")
    @Override
    public ResponseEntity<Void> validateTrade(@Valid @RequestBody ApplyTradeRequest request) {
        authorization.assertServiceCall();
        portfolioService.validateTrade(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/holdings/{holdingId}")
    @Override
    public ResponseEntity<Void> deleteHolding(@PathVariable Long holdingId) {
        authorization.assertManager();
        portfolioService.deleteHolding(holdingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioAccountId}/summary")
    @Override
    public PortfolioSummaryResponse getSummary(@PathVariable Long portfolioAccountId) {
        authorization.assertCanAccessAccount(portfolioAccountId);
        return portfolioService.getSummary(portfolioAccountId);
    }
}
