package com.example.usermicroservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.example.usermicroservice.enums.KycStatus;
import com.example.usermicroservice.enums.RiskLevel;

import java.time.LocalDate;

public record CreateUserDetailRequest(
        @NotNull @Positive Long userId,
        LocalDate dateOfBirth,
        RiskLevel riskLevel,
        Integer riskScore,
        KycStatus kycStatus
) {
}
