package com.example.productmicroservice.controllers;

import com.example.productmicroservice.dto.request.CreateProductTypeRequest;
import com.example.productmicroservice.dto.response.ProductTypeResponse;
import com.example.productmicroservice.enums.ProductStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IProductTypeController {

    ResponseEntity<ProductTypeResponse> create(CreateProductTypeRequest request);

    ProductTypeResponse getById(Long id);

    List<ProductTypeResponse> getAll(ProductStatus status, String typeName);

    ProductTypeResponse update(Long id, CreateProductTypeRequest request);

    ResponseEntity<Void> delete(Long id);
}
