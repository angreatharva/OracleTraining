package com.example.productmicroservice.dto.response;

import com.example.productmicroservice.enums.ProductStatus;

import java.time.LocalDateTime;

public record ProductTypeResponse(
        Long productTypeId,
        String typeCode,
        String typeName,
        String description,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
