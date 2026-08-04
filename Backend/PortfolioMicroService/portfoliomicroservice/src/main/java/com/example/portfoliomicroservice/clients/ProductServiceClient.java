package com.example.portfoliomicroservice.clients;

import com.example.portfoliomicroservice.clients.model.ProductResponse;
import com.example.portfoliomicroservice.exceptions.BusinessRuleException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long productId);

    default void validateProduct(Long productId) {
        try {
            ProductResponse response = getProductById(productId);
            if (response == null || response.productId() == null) {
                throw new BusinessRuleException("Product " + productId + " is not valid or unavailable");
            }
            if (response.status() != null && !"ACTIVE".equalsIgnoreCase(response.status())) {
                throw new BusinessRuleException("Product " + productId + " is not valid or unavailable");
            }
        } catch (FeignException.NotFound ex) {
            throw new BusinessRuleException("Product " + productId + " is not valid or unavailable");
        } catch (FeignException ex) {
            throw new BusinessRuleException("Product Service is currently unavailable");
        }
    }
}
