package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;

import java.time.LocalDate;
import java.util.List;

public interface IPortfolioStatementService {

    PortfolioStatementResponse create(CreatePortfolioStatementRequest request);

    PortfolioStatementResponse getById(Long id);

    List<PortfolioStatementResponse> getAll(Long portfolioAccountId, String status,
                                            LocalDate startDate, LocalDate endDate);
}
