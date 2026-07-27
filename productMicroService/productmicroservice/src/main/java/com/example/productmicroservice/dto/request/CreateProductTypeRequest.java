package com.example.productmicroservice.dto.request;

import com.example.productmicroservice.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductTypeRequest(
        @NotBlank @Size(max = 255) String typeCode,
        @NotBlank @Size(max = 255) String typeName,
        @Size(max = 255) String description,
        ProductStatus status
) {
}
