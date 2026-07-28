package com.example.portfoliomicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** A trusted internal command from Trading to apply an already-funded trade. */
public record ApplyTradeRequest(
        @NotNull @Positive Long portfolioAccountId,
        @NotNull @Positive Long holdingId,
        @NotNull @Positive Long productId,
        @NotBlank String transactionType,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotNull @DecimalMin("0.0001") BigDecimal unitPrice,
        @NotNull @Positive Long transactionId
) {
}
