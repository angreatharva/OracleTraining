package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.DebitRequest;
import com.example.tradingmicroservice.clients.model.DebitResult;

/**
 * Contract to be implemented once Bank Service publishes its debit API.
 */
public interface BankServiceClient {

    DebitResult authorizeDebit(DebitRequest request);
}
