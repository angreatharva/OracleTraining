package com.example.tradingmicroservice.clients;

import com.example.tradingmicroservice.clients.model.HoldingUpdateRequest;

/**
 * Contract to be implemented once Portfolio Service publishes its holding-update API.
 */
public interface PortfolioServiceClient {

    void applyCompletedTrade(HoldingUpdateRequest request);
}
