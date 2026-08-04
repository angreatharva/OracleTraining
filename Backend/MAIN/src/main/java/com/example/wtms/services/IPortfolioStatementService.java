package com.example.wtms.Services;

import com.example.wtms.Entities.PortfolioStatement;

import java.time.LocalDate;
import java.util.List;

public interface IPortfolioStatementService {

    PortfolioStatement create(PortfolioStatement statement);

    PortfolioStatement getById(Long id);

    List<PortfolioStatement> getAll();

    List<PortfolioStatement> getByStatus(String status);

    List<PortfolioStatement> getByPortfolioAccountId(Long portfolioAccountId);

    List<PortfolioStatement> getByHoldingId(Long holdingId);

    List<PortfolioStatement> getByStatementStartBetween(LocalDate startDate, LocalDate endDate);

    PortfolioStatement update(Long id, PortfolioStatement statement);

    void delete(Long id);
}
