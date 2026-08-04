package com.example.tradingmicroservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeTransactionResponse(
        Long transactionId,
        Long portfolioAccountId,
        Long holdingId,
        Long productId,
        String transactionType,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String transactionStatus,
        LocalDateTime transactionDate,
        String failureReason
) {
}
