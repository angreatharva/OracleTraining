package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.ProductQuote;

/**
 * Contract to be implemented once Product Service publishes its API.
 */
public interface ProductServiceClient {

    ProductQuote getActiveProduct(Long productId);
}
