package com.example.tradingmicroservice.clients.model;

public record CreditResult(
        boolean successful,
        String reference,
        String failureReason
) {
}
