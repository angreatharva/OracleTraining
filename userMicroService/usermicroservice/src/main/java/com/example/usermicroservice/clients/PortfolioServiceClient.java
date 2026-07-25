package com.example.usermicroservice.clients;

import com.example.usermicroservice.clients.model.PortfolioAccountSummary;

/**
 * Contract to be implemented when Portfolio Service publishes its account lookup API.
 */
public interface PortfolioServiceClient {

    PortfolioAccountSummary getAccountByUserId(Long userId);
}
