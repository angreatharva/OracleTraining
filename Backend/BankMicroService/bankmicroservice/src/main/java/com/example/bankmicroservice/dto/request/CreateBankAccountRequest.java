package com.example.bankmicroservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * {@code bankName}, {@code accountNumber}, {@code accountType} and {@code ifscCode} are all
 * optional. This lets onboarding open an account for a brand-new investor with nothing but a
 * {@code userId} - {@link com.example.bankmicroservice.services.BankAccountService} fills in
 * a house-bank default for anything left blank, and generates a unique account number itself.
 * A caller that already knows the investor's real bank details may still supply them, and
 * they are used as given.
 */
public record CreateBankAccountRequest(
        @NotNull @Positive Long userId,
        @Size(max = 120) String bankName,
        @Size(max = 120) String branchName,
        @Size(max = 50) String accountNumber,
        String accountType,
        @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "must be a valid IFSC code")
        String ifscCode,
        @DecimalMin(value = "0.00") BigDecimal openingBalance,
        Boolean primaryAccount
) {
}
