package com.example.productmicroservice.services;

import com.example.productmicroservice.dto.request.CreateProductTypeRequest;
import com.example.productmicroservice.dto.response.ProductTypeResponse;
import com.example.productmicroservice.enums.ProductStatus;

import java.util.List;

public interface IProductTypeService {

    ProductTypeResponse create(CreateProductTypeRequest request);

    ProductTypeResponse getById(Long id);

    List<ProductTypeResponse> getAll(ProductStatus status, String typeName);

    ProductTypeResponse update(Long id, CreateProductTypeRequest request);

    void delete(Long id);
}
