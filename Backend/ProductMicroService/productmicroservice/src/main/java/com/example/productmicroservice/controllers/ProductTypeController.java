package com.example.productmicroservice.controllers;

import com.example.productmicroservice.security.AuthorizationHelper;
import com.example.productmicroservice.services.IProductTypeService;
import com.example.productmicroservice.dto.request.CreateProductTypeRequest;
import com.example.productmicroservice.dto.response.ProductTypeResponse;
import com.example.productmicroservice.enums.ProductStatus;
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
@RequestMapping("/api/product-types")
public class ProductTypeController implements IProductTypeController {

    private final IProductTypeService productTypeService;
    private final AuthorizationHelper authorization;

    public ProductTypeController(IProductTypeService productTypeService, AuthorizationHelper authorization) {
        this.productTypeService = productTypeService;
        this.authorization = authorization;
    }

    @PostMapping
    @Override
    public ResponseEntity<ProductTypeResponse> create(
            @Valid @RequestBody CreateProductTypeRequest request) {
        authorization.assertManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(productTypeService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public ProductTypeResponse getById(@PathVariable Long id) {
        return productTypeService.getById(id);
    }

    @GetMapping
    @Override
    public List<ProductTypeResponse> getAll(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String typeName) {
        return productTypeService.getAll(status, typeName);
    }

    @PutMapping("/{id}")
    @Override
    public ProductTypeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductTypeRequest request) {
        authorization.assertManager();
        return productTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorization.assertManager();
        productTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
