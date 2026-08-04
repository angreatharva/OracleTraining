package com.example.portfoliomicroservice.controllers;

import com.example.portfoliomicroservice.dto.request.CreateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.CreatePortfolioAccountRequest;
import com.example.portfoliomicroservice.dto.request.UpdateHoldingRequest;
import com.example.portfoliomicroservice.dto.request.UpdatePortfolioStatusRequest;
import com.example.portfoliomicroservice.dto.request.ApplyTradeRequest;
import com.example.portfoliomicroservice.dto.response.PortfolioAccountResponse;
import com.example.portfoliomicroservice.dto.response.PortfolioHoldingResponse;
import org.springframework.http.ResponseEntity;
import com.example.portfoliomicroservice.dto.response.PortfolioSummaryResponse;
import com.example.portfoliomicroservice.enums.HoldingStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IPortfolioController {
    ResponseEntity<PortfolioAccountResponse> createAccount(CreatePortfolioAccountRequest request);
    List<PortfolioAccountResponse> getAccounts();
    PortfolioAccountResponse getAccount(Long portfolioAccountId);
    PortfolioAccountResponse getAccountByUser(Long userId);
    PortfolioAccountResponse updateStatus(Long portfolioAccountId, UpdatePortfolioStatusRequest request);
    ResponseEntity<PortfolioHoldingResponse> addHolding(Long portfolioAccountId, CreateHoldingRequest request);
    List<PortfolioHoldingResponse> getHoldings(Long portfolioAccountId, HoldingStatus status);
    PortfolioHoldingResponse getHolding(Long holdingId);
    PortfolioHoldingResponse updateHolding(Long holdingId, UpdateHoldingRequest request);
    ResponseEntity<Void> applyCompletedTrade(ApplyTradeRequest request);

    ResponseEntity<Void> validateTrade(ApplyTradeRequest request);
    ResponseEntity<Void> deleteHolding(Long holdingId);
    PortfolioSummaryResponse getSummary(Long portfolioAccountId);
}
