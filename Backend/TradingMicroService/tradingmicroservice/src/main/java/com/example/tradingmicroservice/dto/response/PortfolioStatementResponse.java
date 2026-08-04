package com.example.tradingmicroservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PortfolioStatementResponse(
        Long statementId,
        Long portfolioAccountId,
        Long holdingId,
        Long transactionId,
        LocalDate statementStart,
        LocalDate statementEnd,
        BigDecimal openingValue,
        BigDecimal closingValue,
        LocalDateTime generatedAt,
        String status,
        List<Long> transactionIds
) {
}
