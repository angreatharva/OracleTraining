package com.example.portfoliomicroservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePortfolioAccountRequest(
        @NotNull Long userId,
        LocalDate openedDate
) {
}
