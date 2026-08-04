package com.example.bankmicroservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateKycDocumentRequest(
        @NotNull @Positive Long userId,
        @NotBlank String documentType,
        @NotBlank @Size(max = 100) String documentNumber,
        @Size(max = 255) String fileName
) {
}
