package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.DebitResult;
import com.example.tradingmicroservice.clients.model.CreditRequest;
import com.example.tradingmicroservice.clients.model.CreditResult;

/**
 * Bank API contract. The implementation uses RestClient and Eureka service discovery.
 */
public interface BankServiceClient {

    DebitResult authorizeDebit(Long bankAccountId, DebitRequest request);

    CreditResult credit(Long bankAccountId, CreditRequest request);
}
