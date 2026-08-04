package com.example.tradingmicroservice.clients.model;

import java.math.BigDecimal;

public record DebitRequest(
        BigDecimal amount,
        String transactionReference
) {
}
