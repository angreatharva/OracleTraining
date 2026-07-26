package com.example.tradingmicroservice.repositories;

import com.example.tradingmicroservice.entities.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    List<TradeTransaction> findByPortfolioAccountId(Long portfolioAccountId);

    List<TradeTransaction> findByProductId(Long productId);

    List<TradeTransaction> findByTransactionStatus(String transactionStatus);

    List<TradeTransaction> findByTransactionType(String transactionType);

    List<TradeTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
