package com.example.tradingmicroservice.clients.model;

import java.math.BigDecimal;

public record CreditRequest(
        BigDecimal amount,
        String transactionReference
) {
}
