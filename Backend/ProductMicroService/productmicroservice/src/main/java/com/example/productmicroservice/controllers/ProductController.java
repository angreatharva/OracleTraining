package com.example.productmicroservice.controllers;

import com.example.productmicroservice.services.IProductService;
import com.example.productmicroservice.dto.request.CreateInvestmentProductRequest;
import com.example.productmicroservice.dto.response.InvestmentProductResponse;
import com.example.productmicroservice.enums.PriceMethod;
import com.example.productmicroservice.enums.ProductStatus;
import com.example.productmicroservice.enums.RiskCategory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController implements IProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Override
    public ResponseEntity<InvestmentProductResponse> create(
            @Valid @RequestBody CreateInvestmentProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public InvestmentProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping
    @Override
    public List<InvestmentProductResponse> getAll(
            @RequestParam(required = false) Long productTypeId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) RiskCategory riskCategory,
            @RequestParam(required = false) PriceMethod priceMethod,
            @RequestParam(required = false) String productName) {
        return productService.getAll(productTypeId, status, riskCategory, priceMethod, productName);
    }

    @PutMapping("/{id}")
    @Override
    public InvestmentProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CreateInvestmentProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
