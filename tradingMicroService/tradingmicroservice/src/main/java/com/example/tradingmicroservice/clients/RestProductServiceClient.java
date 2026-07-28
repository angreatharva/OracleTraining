package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.ProductQuote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestProductServiceClient implements ProductServiceClient {

    private final RestClient restClient;

    public RestProductServiceClient(RestClient.Builder loadBalancedRestClientBuilder,
                                    @Value("${clients.product.base-url:http://PRODUCT-SERVICE}") String baseUrl) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public ProductQuote getProduct(Long productId) {
        return restClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .body(ProductQuote.class);
    }
}
