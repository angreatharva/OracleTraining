package com.example.portfoliomicroservice.dto.request;

import com.example.portfoliomicroservice.enums.HoldingStatus;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateHoldingRequest(
        @DecimalMin(value = "0.0000") BigDecimal quantity,
        @DecimalMin(value = "0.0000") BigDecimal averageCost,
        HoldingStatus holdingStatus
) {
}
