package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioStatementRepository extends JpaRepository<PortfolioStatement, Long> {

    List<PortfolioStatement> findByPortfolioAccount_PortfolioAccountId(Long portfolioAccountId);

    List<PortfolioStatement> findByHolding_HoldingId(Long holdingId);

    List<PortfolioStatement> findByStatus(String status);

    List<PortfolioStatement> findByStatementStartBetween(LocalDate startDate, LocalDate endDate);
}