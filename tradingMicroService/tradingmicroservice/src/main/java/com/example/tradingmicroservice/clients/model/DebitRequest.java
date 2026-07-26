package com.example.tradingmicroservice.clients.model;

import java.math.BigDecimal;

public record DebitRequest(
        Long portfolioAccountId,
        BigDecimal amount,
        String transactionReference
) {
}
