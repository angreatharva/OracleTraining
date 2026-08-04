package com.example.bankmicroservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateKycVerificationRequest(
        @NotBlank String verificationStatus
) {
}
