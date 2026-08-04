package com.example.tradingmicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTradeTransactionRequest(
        @NotNull @Positive Long portfolioAccountId,
        @NotNull @Positive Long holdingId,
        @NotNull @Positive Long productId,
        @NotNull @Positive Long bankAccountId,
        @NotBlank String transactionType,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice
) {
}
