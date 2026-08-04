package com.example.productmicroservice.dto.response;

import com.example.productmicroservice.enums.PriceMethod;
import com.example.productmicroservice.enums.ProductStatus;
import com.example.productmicroservice.enums.RiskCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvestmentProductResponse(
        Long productId,
        Long productTypeId,
        String productTypeCode,
        String productName,
        BigDecimal basePrice,
        BigDecimal currentPrice,
        BigDecimal minimumInvestment,
        RiskCategory riskCategory,
        PriceMethod priceMethod,
        Integer tenureMonths,
        BigDecimal interestRate,
        LocalDate issueDate,
        LocalDate maturityDate,
        ProductStatus status,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
