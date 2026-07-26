package com.example.bankmicroservice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record KycDocumentResponse(
        Long kycDocumentId,
        Long userId,
        String documentType,
        String maskedDocumentNumber,
        String fileName,
        String verificationStatus,
        LocalDate submittedDate,
        LocalDate verifiedDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
