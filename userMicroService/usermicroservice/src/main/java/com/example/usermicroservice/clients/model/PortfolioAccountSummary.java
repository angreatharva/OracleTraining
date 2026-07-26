package com.example.usermicroservice.clients.model;

public record PortfolioAccountSummary(
        Long portfolioAccountId,
        Long userId,
        String accountStatus
) {
}
