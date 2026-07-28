package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.ProductQuote;

/**
 * Product API contract. The implementation uses RestClient and Eureka service discovery.
 */
public interface ProductServiceClient {

    ProductQuote getProduct(Long productId);

    default ProductQuote getActiveProduct(Long productId) {
        ProductQuote product = getProduct(productId);
        if (product == null || !product.active() || product.currentPrice() == null
                || product.currentPrice().signum() <= 0) {
            throw new IllegalArgumentException("Product " + productId + " is not available for trading");
        }
        return product;
    }
}
