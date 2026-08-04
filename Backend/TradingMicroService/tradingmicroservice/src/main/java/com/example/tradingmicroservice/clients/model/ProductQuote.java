package com.example.tradingmicroservice.clients.model;

import java.math.BigDecimal;

public record ProductQuote(
        Long productId,
        String productName,
        BigDecimal currentPrice,
        boolean active
) {
}
