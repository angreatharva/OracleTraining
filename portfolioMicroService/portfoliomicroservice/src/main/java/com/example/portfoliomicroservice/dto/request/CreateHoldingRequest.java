package com.example.portfoliomicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateHoldingRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.0000") BigDecimal averageCost
) {
}
