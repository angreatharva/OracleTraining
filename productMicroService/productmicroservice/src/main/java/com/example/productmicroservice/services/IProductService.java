package com.example.productmicroservice.services;

import com.example.productmicroservice.dto.request.CreateInvestmentProductRequest;
import com.example.productmicroservice.dto.response.InvestmentProductResponse;
import com.example.productmicroservice.enums.PriceMethod;
import com.example.productmicroservice.enums.ProductStatus;
import com.example.productmicroservice.enums.RiskCategory;

import java.util.List;

public interface IProductService {

    InvestmentProductResponse create(CreateInvestmentProductRequest request);

    InvestmentProductResponse getById(Long id);

    List<InvestmentProductResponse> getAll(Long productTypeId, ProductStatus status,
                                           RiskCategory riskCategory, PriceMethod priceMethod,
                                           String productName);

    InvestmentProductResponse update(Long id, CreateInvestmentProductRequest request);

    void delete(Long id);
}
