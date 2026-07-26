package com.example.wtms.Services;

import com.example.wtms.Entities.TradeTransaction;

import java.util.List;

public interface ITradeTransactionService {

    TradeTransaction create(TradeTransaction transaction);

    TradeTransaction getById(Long id);

    List<TradeTransaction> getAll();

    List<TradeTransaction> getByStatus(String status);

    List<TradeTransaction> getByType(String type);

    List<TradeTransaction> getByPortfolioAccountId(Long portfolioAccountId);

    List<TradeTransaction> getByHoldingId(Long holdingId);

    List<TradeTransaction> getByProductId(Long productId);

    TradeTransaction update(Long id, TradeTransaction transaction);

    void delete(Long id);
}
