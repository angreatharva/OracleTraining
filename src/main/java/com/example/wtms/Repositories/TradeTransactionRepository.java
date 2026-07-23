package com.example.wtms.Repositories;

import com.example.wtms.Entities.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    List<TradeTransaction> findByTransactionStatus(String transactionStatus);

    List<TradeTransaction> findByTransactionType(String transactionType);

    List<TradeTransaction> findByPortfolioAccount_PortfolioAccountId(Long portfolioAccountId);

    List<TradeTransaction> findByHolding_HoldingId(Long holdingId);

    List<TradeTransaction> findByProduct_ProductId(Long productId);
}