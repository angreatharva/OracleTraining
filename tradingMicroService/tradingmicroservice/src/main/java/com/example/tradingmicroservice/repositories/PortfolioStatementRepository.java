package com.example.tradingmicroservice.repositories;

import com.example.tradingmicroservice.entities.PortfolioStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioStatementRepository extends JpaRepository<PortfolioStatement, Long> {

    List<PortfolioStatement> findByPortfolioAccountId(Long portfolioAccountId);

    List<PortfolioStatement> findByHoldingId(Long holdingId);

    List<PortfolioStatement> findByTransactionId(Long transactionId);

    List<PortfolioStatement> findByStatus(String status);

    List<PortfolioStatement> findByStatementStartBetween(LocalDate startDate, LocalDate endDate);
}
