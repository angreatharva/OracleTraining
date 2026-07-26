package com.example.tradingmicroservice.clients.model;

import java.math.BigDecimal;

public record HoldingUpdateRequest(
        Long portfolioAccountId,
        Long holdingId,
        Long productId,
        String transactionType,
        BigDecimal quantity,
        BigDecimal unitPrice,
        Long transactionId
) {
}
