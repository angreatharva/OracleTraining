package com.example.portfoliomicroservice.dto.response;

import com.example.portfoliomicroservice.enums.HoldingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortfolioHoldingResponse(
        Long holdingId,
        Long portfolioAccountId,
        Long productId,
        BigDecimal quantity,
        BigDecimal averageCost,
        BigDecimal marketValue,
        BigDecimal unrealizedGainLoss,
        HoldingStatus holdingStatus,
        LocalDateTime lastValuedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
