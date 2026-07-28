package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.CreditRequest;
import com.example.tradingmicroservice.clients.model.CreditResult;
import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.DebitResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestBankServiceClient implements BankServiceClient {

    private final RestClient restClient;

    public RestBankServiceClient(RestClient.Builder loadBalancedRestClientBuilder,
                                 @Value("${clients.bank.base-url:http://BANK-SERVICE}") String baseUrl) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public DebitResult authorizeDebit(Long bankAccountId, DebitRequest request) {
        return restClient.post()
                .uri("/api/bank-accounts/{id}/debit", bankAccountId)
                .body(request)
                .retrieve()
                .body(DebitResult.class);
    }

    @Override
    public CreditResult credit(Long bankAccountId, CreditRequest request) {
        return restClient.post()
                .uri("/api/bank-accounts/{id}/credit", bankAccountId)
                .body(request)
                .retrieve()
                .body(CreditResult.class);
    }
}
