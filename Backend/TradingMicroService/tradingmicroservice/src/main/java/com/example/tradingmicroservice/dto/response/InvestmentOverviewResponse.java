package com.example.tradingmicroservice.dto.response;

import java.util.List;

public record InvestmentOverviewResponse(
        Long userId,
        Long portfolioAccountId,
        List<InvestmentPositionResponse> positions
) {
}
