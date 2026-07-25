package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface IPortfolioStatementController {

    ResponseEntity<PortfolioStatementResponse> create(CreatePortfolioStatementRequest request);

    PortfolioStatementResponse getById(Long id);

    List<PortfolioStatementResponse> getAll(Long portfolioAccountId, String status,
                                            LocalDate startDate, LocalDate endDate);
}
