package com.example.productmicroservice.dto.request;

import com.example.productmicroservice.enums.PriceMethod;
import com.example.productmicroservice.enums.ProductStatus;
import com.example.productmicroservice.enums.RiskCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInvestmentProductRequest(
        @NotNull @Positive Long productTypeId,
        @NotBlank @Size(max = 255) String productName,
        @NotNull @DecimalMin("0.01") BigDecimal basePrice,
        @NotNull @DecimalMin("0.01") BigDecimal currentPrice,
        @NotNull @DecimalMin("0.01") BigDecimal minimumInvestment,
        @NotNull RiskCategory riskCategory,
        @NotNull PriceMethod priceMethod,
        @Positive Integer tenureMonths,
        @DecimalMin("0.0") BigDecimal interestRate,
        LocalDate issueDate,
        LocalDate maturityDate,
        ProductStatus status
) {
}
