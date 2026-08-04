package com.example.bankmicroservice.dto.response;

public record CreditResult(
        boolean successful,
        String reference,
        String failureReason
) {
}
