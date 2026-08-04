package com.example.usermicroservice.dto.response;

import java.time.LocalDate;
import com.example.usermicroservice.enums.KycStatus;
import com.example.usermicroservice.enums.RiskLevel;

public record UserDetailResponse(
        Long userDetailId,
        Long userId,
        LocalDate dateOfBirth,
        RiskLevel riskLevel,
        Integer riskScore,
        KycStatus kycStatus
) {
}
