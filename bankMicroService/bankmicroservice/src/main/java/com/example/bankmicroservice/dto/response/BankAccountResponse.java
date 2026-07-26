package com.example.bankmicroservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BankAccountResponse(
        Long bankAccountId,
        Long userId,
        String bankName,
        String branchName,
        String maskedAccountNumber,
        String accountType,
        String ifscCode,
        BigDecimal balance,
        boolean primaryAccount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
