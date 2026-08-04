package com.example.portfoliomicroservice.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummaryResponse(
        PortfolioAccountResponse portfolioAccount,
        List<PortfolioHoldingResponse> holdings,
        BigDecimal totalCost,
        BigDecimal marketValue,
        BigDecimal unrealizedGainLoss
) {
}
