package com.example.portfoliomicroservice.services;

import com.example.portfoliomicroservice.dto.request.CreateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.dto.request.UpdateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.UpdatePortfolioStatusRequest;
import com.example.portfoliomicroservice.dto.request.ApplyTradeRequest;
import com.example.portfoliomicroservice.dto.response.PortfolioAccountResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioHoldingResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioSummaryResponse;
import com.example.portfoliomicroservice.enums.HoldingStatus;

import java.util.List;

public interface PortfolioService {
    PortfolioAccountResponse createAccount(CreatePortfolioAccountRequest request);
    PortfolioAccountResponse getAccount(Long portfolioAccountId);
    PortfolioAccountResponse getAccountByUser(Long userId);
    List<PortfolioAccountResponse> getAccounts();
    PortfolioAccountResponse updateStatus(Long portfolioAccountId, UpdatePortfolioStatusRequest request);
    PortfolioHoldingResponse addHolding(Long portfolioAccountId, CreateHoldingRequest request);
    PortfolioHoldingResponse getHolding(Long holdingId);
    List<PortfolioHoldingResponse> getHoldings(Long portfolioAccountId, HoldingStatus status);
    PortfolioHoldingResponse updateHolding(Long holdingId, UpdateHoldingRequest request);
    void validateTrade(ApplyTradeRequest request);
    PortfolioHoldingResponse applyCompletedTrade(ApplyTradeRequest request);
    void deleteHolding(Long holdingId);
    PortfolioSummaryResponse getSummary(Long portfolioAccountId);
}
