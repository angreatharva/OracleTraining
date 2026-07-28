package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import com.example.tradingmicroservice.clients.model.PortfolioHoldingSnapshot;

import java.util.List;

/**
 * Portfolio API contract. The implementation uses RestClient and Eureka service discovery.
 */
public interface PortfolioServiceClient {

    void validateTrade(HoldingUpdateRequest request);

    void applyCompletedTrade(HoldingUpdateRequest request);

    PortfolioAccountSummary getAccountByUser(Long userId);

    List<PortfolioHoldingSnapshot> getHoldings(Long portfolioAccountId);
}
