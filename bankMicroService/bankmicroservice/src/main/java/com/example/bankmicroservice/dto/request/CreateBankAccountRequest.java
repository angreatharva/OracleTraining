package com.example.bankmicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateBankAccountRequest(
        @NotNull @Positive Long userId,
        @NotBlank @Size(max = 120) String bankName,
        @Size(max = 120) String branchName,
        @NotBlank @Size(max = 50) String accountNumber,
        @NotBlank String accountType,
        @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "must be a valid IFSC code")
        String ifscCode,
        @DecimalMin(value = "0.00") BigDecimal openingBalance,
        Boolean primaryAccount
) {
}
