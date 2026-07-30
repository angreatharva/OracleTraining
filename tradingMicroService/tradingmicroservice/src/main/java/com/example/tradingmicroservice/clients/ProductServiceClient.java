package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.ProductQuote;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Product API contract resolved through Eureka by service name.
 */
@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}")
    ProductQuote getProduct(@PathVariable("productId") Long productId);

    default ProductQuote getActiveProduct(Long productId) {
        ProductQuote product = getProduct(productId);
        if (product == null || !product.active() || product.currentPrice() == null
                || product.currentPrice().signum() <= 0) {
            throw new IllegalArgumentException("Product " + productId + " is not available for trading");
        }
        return product;
    }
}
