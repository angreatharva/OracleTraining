package com.example.usermicroservice.clients.model;

import java.math.BigDecimal;

public record BankAccountSummary(
        Long bankAccountId,
        Long userId,
        BigDecimal balance,
        String status
) {
}
