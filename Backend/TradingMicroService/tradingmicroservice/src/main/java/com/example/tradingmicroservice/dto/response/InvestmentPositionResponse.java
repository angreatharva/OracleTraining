package com.example.tradingmicroservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestmentPositionResponse(
        Long productId,
        String productName,
        BigDecimal boughtQuantity,
        BigDecimal soldQuantity,
        BigDecimal currentQuantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal investedValue,
        BigDecimal currentValuation,
        BigDecimal profitLoss,
        LocalDateTime lastTransactionDate
) {
}
