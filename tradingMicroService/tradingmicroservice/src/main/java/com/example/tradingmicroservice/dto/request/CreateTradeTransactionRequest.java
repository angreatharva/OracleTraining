package com.example.tradingmicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTradeTransactionRequest(
        @NotNull @Positive Long portfolioAccountId,
        @NotNull @Positive Long holdingId,
        @NotNull @Positive Long productId,
        @NotBlank String transactionType,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice,
        LocalDateTime transactionDate
) {
}
