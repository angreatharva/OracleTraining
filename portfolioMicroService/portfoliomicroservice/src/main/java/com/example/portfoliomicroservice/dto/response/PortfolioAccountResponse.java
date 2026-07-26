package com.example.portfoliomicroservice.dto.response;

import com.example.portfoliomicroservice.enums.AccountStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PortfolioAccountResponse(
        Long portfolioAccountId,
        Long userId,
        AccountStatus accountStatus,
        LocalDate openedDate,
        LocalDate closedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
