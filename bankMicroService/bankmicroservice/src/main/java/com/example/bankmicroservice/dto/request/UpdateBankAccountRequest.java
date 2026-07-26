package com.example.bankmicroservice.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBankAccountRequest(
        @Size(min = 1, max = 120) String bankName,
        @Size(max = 120) String branchName,
        String accountType,
        @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "must be a valid IFSC code")
        String ifscCode,
        String status
) {
}
