package com.example.portfoliomicroservice.repositories;

import com.example.portfoliomicroservice.entities.PortfolioHolding;
import com.example.portfoliomicroservice.enums.HoldingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {
    List<PortfolioHolding> findByPortfolioAccountPortfolioAccountId(Long portfolioAccountId);
    List<PortfolioHolding> findByPortfolioAccountPortfolioAccountIdAndHoldingStatus(Long portfolioAccountId, HoldingStatus holdingStatus);
    Optional<PortfolioHolding> findByPortfolioAccountPortfolioAccountIdAndProductId(Long portfolioAccountId, Long productId);
    List<PortfolioHolding> findByProductId(Long productId);
}
