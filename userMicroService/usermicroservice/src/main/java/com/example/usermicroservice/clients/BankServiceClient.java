package com.example.usermicroservice.clients;

import com.example.usermicroservice.clients.model.BankAccountSummary;

/**
 * Contract to be implemented when Bank Service publishes its account lookup API.
 */
public interface BankServiceClient {

    BankAccountSummary getPrimaryAccount(Long userId);
}
