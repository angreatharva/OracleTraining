package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PortfolioStatementRepository extends JpaRepository<PortfolioStatement, Long> {

    List<PortfolioStatement> findByPortfolioAccountPortfolioAccountId(Long portfolioAccountId);

    List<PortfolioStatement> findByHoldingHoldingId(Long holdingId);

    List<PortfolioStatement> findByStatus(String status);

    List<PortfolioStatement> findByStatementStartBetween(LocalDate startDate, LocalDate endDate);
}