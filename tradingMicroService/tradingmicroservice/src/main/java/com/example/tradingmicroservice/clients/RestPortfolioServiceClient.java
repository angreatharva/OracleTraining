package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;
import com.example.tradingmicroservice.clients.model.PortfolioAccountSummary;
import com.example.tradingmicroservice.clients.model.PortfolioHoldingSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RestPortfolioServiceClient implements PortfolioServiceClient {

    private final RestClient restClient;

    public RestPortfolioServiceClient(@Qualifier("portfolioRestClientBuilder") RestClient.Builder portfolioRestClientBuilder,
                                      @Value("${clients.portfolio.base-url:http://localhost:8084}") String baseUrl) {
        this.restClient = portfolioRestClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public void validateTrade(HoldingUpdateRequest request) {
        restClient.post()
                .uri("/api/portfolios/internal/trades/validate")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void applyCompletedTrade(HoldingUpdateRequest request) {
        restClient.post()
                .uri("/api/portfolios/internal/trades")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public PortfolioAccountSummary getAccountByUser(Long userId) {
        return restClient.get()
                .uri("/api/portfolios/by-user/{userId}", userId)
                .retrieve()
                .body(PortfolioAccountSummary.class);
    }

    @Override
    public List<PortfolioHoldingSnapshot> getHoldings(Long portfolioAccountId) {
        PortfolioHoldingSnapshot[] holdings = restClient.get()
                .uri("/api/portfolios/{id}/holdings", portfolioAccountId)
                .retrieve()
                .body(PortfolioHoldingSnapshot[].class);
        return holdings == null ? List.of() : List.of(holdings);
    }
}
