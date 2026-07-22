package com.example.wtms.Repositories;

import com.example.wtms.Entities.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

    List<PortfolioHolding> findByPortfolioAccountPortfolioAccountId(Long portfolioAccountId);

    List<PortfolioHolding> findByProductProductTypeId(Long productTypeId);

    List<PortfolioHolding> findByPortfolioAccountPortfolioAccountIdAndHoldingStatus(
            Long portfolioAccountId,
            String holdingStatus
    );
}