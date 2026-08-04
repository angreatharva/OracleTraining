package com.example.tradingmicroservice.services;

import com.example.tradingmicroservice.clients.PortfolioServiceClient;
import com.example.tradingmicroservice.clients.ProductServiceClient;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import com.example.tradingmicroservice.clients.model.PortfolioHoldingSnapshot;
import com.example.tradingmicroservice.clients.model.ProductQuote;
import com.example.tradingmicroservice.dto.response.InvestmentOverviewResponse;
import com.example.tradingmicroservice.dto.response.InvestmentPositionResponse;
import com.example.tradingmicroservice.entities.TradeTransaction;
import com.example.tradingmicroservice.repositories.TradeTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class InvestmentOverviewService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);

    private final TradeTransactionRepository transactionRepository;
    private final PortfolioServiceClient portfolioServiceClient;
    private final ProductServiceClient productServiceClient;

    public InvestmentOverviewService(TradeTransactionRepository transactionRepository,
                                     PortfolioServiceClient portfolioServiceClient,
                                     ProductServiceClient productServiceClient) {
        this.transactionRepository = transactionRepository;
        this.portfolioServiceClient = portfolioServiceClient;
        this.productServiceClient = productServiceClient;
    }

    public InvestmentOverviewResponse getForUser(Long userId) {
        PortfolioAccountSummary account = portfolioServiceClient.getAccountByUser(userId);
        if (account == null || account.portfolioAccountId() == null) {
            throw new IllegalArgumentException("No portfolio account found for user " + userId);
        }

        List<TradeTransaction> completedTrades = transactionRepository
                .findByPortfolioAccountId(account.portfolioAccountId()).stream()
                .filter(trade -> "COMPLETED".equalsIgnoreCase(trade.getTransactionStatus()))
                .toList();

        Map<Long, PortfolioHoldingSnapshot> holdingsByProduct = new LinkedHashMap<>();
        for (PortfolioHoldingSnapshot holding : portfolioServiceClient.getHoldings(account.portfolioAccountId())) {
            holdingsByProduct.put(holding.productId(), holding);
        }

        Map<Long, List<TradeTransaction>> tradesByProduct = new LinkedHashMap<>();
        for (TradeTransaction trade : completedTrades) {
            tradesByProduct.computeIfAbsent(trade.getProductId(), ignored -> new java.util.ArrayList<>()).add(trade);
        }
        holdingsByProduct.keySet().forEach(productId -> tradesByProduct.putIfAbsent(productId, List.of()));

        List<InvestmentPositionResponse> positions = tradesByProduct.entrySet().stream()
                .map(entry -> toPosition(entry.getKey(), entry.getValue(), holdingsByProduct.get(entry.getKey())))
                .sorted(Comparator.comparing(InvestmentPositionResponse::productName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return new InvestmentOverviewResponse(userId, account.portfolioAccountId(), positions);
    }

    private InvestmentPositionResponse toPosition(Long productId, List<TradeTransaction> trades,
                                                   PortfolioHoldingSnapshot holding) {
        BigDecimal boughtQuantity = sumQuantity(trades, "BUY");
        BigDecimal soldQuantity = sumQuantity(trades, "SELL");
        BigDecimal buyAmount = sumAmount(trades, "BUY");
        BigDecimal averageBuyPrice = boughtQuantity.signum() == 0 ? ZERO
                : buyAmount.divide(boughtQuantity, 4, RoundingMode.HALF_UP);
        BigDecimal currentQuantity = holding == null ? ZERO : safe(holding.quantity());
        BigDecimal investedValue = currentQuantity.multiply(averageBuyPrice).setScale(4, RoundingMode.HALF_UP);

        ProductQuote product = productServiceClient.getProduct(productId);
        BigDecimal currentPrice = safe(product.currentPrice());
        BigDecimal currentValuation = currentQuantity.multiply(currentPrice).setScale(4, RoundingMode.HALF_UP);
        LocalDateTime lastTransactionDate = trades.stream().map(TradeTransaction::getTransactionDate)
                .filter(java.util.Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);

        return new InvestmentPositionResponse(
                productId, product.productName(), boughtQuantity, soldQuantity, currentQuantity,
                averageBuyPrice, currentPrice, investedValue, currentValuation,
                currentValuation.subtract(investedValue).setScale(4, RoundingMode.HALF_UP), lastTransactionDate);
    }

    private BigDecimal sumQuantity(List<TradeTransaction> trades, String type) {
        return trades.stream().filter(trade -> type.equalsIgnoreCase(trade.getTransactionType()))
                .map(TradeTransaction::getQuantity).map(this::safe).reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal sumAmount(List<TradeTransaction> trades, String type) {
        return trades.stream().filter(trade -> type.equalsIgnoreCase(trade.getTransactionType()))
                .map(TradeTransaction::getTotalAmount).map(this::safe).reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
