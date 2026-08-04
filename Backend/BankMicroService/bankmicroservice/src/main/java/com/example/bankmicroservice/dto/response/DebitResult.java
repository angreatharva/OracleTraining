package com.example.bankmicroservice.dto.response;

public record DebitResult(
        boolean approved,
        String reference,
        String failureReason
) {
}
