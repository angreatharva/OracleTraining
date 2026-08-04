package com.example.portfoliomicroservice.dto.request;

import com.example.portfoliomicroservice.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePortfolioStatusRequest(
        @NotNull AccountStatus accountStatus
) {
}
