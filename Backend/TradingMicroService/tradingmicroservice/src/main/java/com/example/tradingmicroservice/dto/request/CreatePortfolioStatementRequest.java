package com.example.tradingmicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreatePortfolioStatementRequest(
        @NotNull @Positive Long portfolioAccountId,
        @NotNull @Positive Long holdingId,
        @NotNull @Positive Long transactionId,
        @NotNull LocalDate statementStart,
        @NotNull LocalDate statementEnd,
        @NotNull @DecimalMin(value = "0.00") BigDecimal openingValue,
        @NotNull @DecimalMin(value = "0.00") BigDecimal closingValue,
        List<Long> transactionIds
) {
}
