package com.example.productmicroservice.controllers;

import com.example.productmicroservice.dto.request.CreateInvestmentProductRequest;
import com.example.productmicroservice.dto.response.InvestmentProductResponse;
import com.example.productmicroservice.enums.PriceMethod;
import com.example.productmicroservice.enums.ProductStatus;
import com.example.productmicroservice.enums.RiskCategory;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IProductController {

    ResponseEntity<InvestmentProductResponse> create(CreateInvestmentProductRequest request);

    InvestmentProductResponse getById(Long id);

    List<InvestmentProductResponse> getAll(Long productTypeId, ProductStatus status,
                                           RiskCategory riskCategory, PriceMethod priceMethod,
                                           String productName);

    InvestmentProductResponse update(Long id, CreateInvestmentProductRequest request);

    ResponseEntity<Void> delete(Long id);
}
