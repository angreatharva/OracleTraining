package com.example.portfoliomicroservice.controllers;

import com.example.portfoliomicroservice.dto.request.CreateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.dto.request.UpdateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.UpdatePortfolioStatusRequest;
import com.example.portfoliomicroservice.dto.response.PortfolioAccountResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioHoldingResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioSummaryResponse;
import com.example.portfoliomicroservice.enums.HoldingStatus;
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

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController implements IPortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    @Override
    public ResponseEntity<PortfolioAccountResponse> createAccount(@Valid @RequestBody CreatePortfolioAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.createAccount(request));
    }

    @GetMapping
    @Override
    public List<PortfolioAccountResponse> getAccounts() {
        return portfolioService.getAccounts();
    }

    @GetMapping("/{portfolioAccountId}")
    @Override
    public PortfolioAccountResponse getAccount(@PathVariable Long portfolioAccountId) {
        return portfolioService.getAccount(portfolioAccountId);
    }

    @GetMapping("/by-user/{userId}")
    @Override
    public PortfolioAccountResponse getAccountByUser(@PathVariable Long userId) {
        return portfolioService.getAccountByUser(userId);
    }

    @PatchMapping("/{portfolioAccountId}/status")
    @Override
    public PortfolioAccountResponse updateStatus(@PathVariable Long portfolioAccountId,
                                                 @Valid @RequestBody UpdatePortfolioStatusRequest request) {
        return portfolioService.updateStatus(portfolioAccountId, request);
    }

    @PostMapping("/{portfolioAccountId}/holdings")
    @Override
    public ResponseEntity<PortfolioHoldingResponse> addHolding(@PathVariable Long portfolioAccountId,
                                                               @Valid @RequestBody CreateHoldingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.addHolding(portfolioAccountId, request));
    }

    @GetMapping("/{portfolioAccountId}/holdings")
    @Override
    public List<PortfolioHoldingResponse> getHoldings(@PathVariable Long portfolioAccountId,
                                                      @RequestParam(required = false) HoldingStatus status) {
        return portfolioService.getHoldings(portfolioAccountId, status);
    }

    @GetMapping("/holdings/{holdingId}")
    @Override
    public PortfolioHoldingResponse getHolding(@PathVariable Long holdingId) {
        return portfolioService.getHolding(holdingId);
    }

    @PatchMapping("/holdings/{holdingId}")
    @Override
    public PortfolioHoldingResponse updateHolding(@PathVariable Long holdingId,
                                                  @Valid @RequestBody UpdateHoldingRequest request) {
        return portfolioService.updateHolding(holdingId, request);
    }

    @DeleteMapping("/holdings/{holdingId}")
    @Override
    public ResponseEntity<Void> deleteHolding(@PathVariable Long holdingId) {
        portfolioService.deleteHolding(holdingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioAccountId}/summary")
    @Override
    public PortfolioSummaryResponse getSummary(@PathVariable Long portfolioAccountId) {
        return portfolioService.getSummary(portfolioAccountId);
    }
}
