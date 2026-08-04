package com.example.tradingmicroservice.clients.model;

public record DebitResult(
        boolean approved,
        String reference,
        String failureReason
) {
}
