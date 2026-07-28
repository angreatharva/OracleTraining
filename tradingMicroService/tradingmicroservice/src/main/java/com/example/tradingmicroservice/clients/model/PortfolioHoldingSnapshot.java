package com.example.tradingmicroservice.clients.model;

import java.math.BigDecimal;

public record PortfolioHoldingSnapshot(
        Long holdingId,
        Long portfolioAccountId,
        Long productId,
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal marketValue
) {
}
