package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import com.example.tradingmicroservice.clients.model.PortfolioHoldingSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Portfolio API contract resolved through Eureka by service name.
 */
@FeignClient(name = "PORTFOLIO-SERVICE")
public interface PortfolioServiceClient {

    @PostMapping("/api/portfolios/internal/trades/validate")
    void validateTrade(@RequestBody HoldingUpdateRequest request);

    @PostMapping("/api/portfolios/internal/trades")
    void applyCompletedTrade(@RequestBody HoldingUpdateRequest request);

    @GetMapping("/api/portfolios/by-user/{userId}")
    PortfolioAccountSummary getAccountByUser(@PathVariable("userId") Long userId);

    @GetMapping("/api/portfolios/{portfolioAccountId}/holdings")
    List<PortfolioHoldingSnapshot> getHoldings(
            @PathVariable("portfolioAccountId") Long portfolioAccountId);
}
