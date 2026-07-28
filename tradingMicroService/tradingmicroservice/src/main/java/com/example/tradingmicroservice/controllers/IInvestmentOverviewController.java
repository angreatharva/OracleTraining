package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.response.InvestmentOverviewResponse;

public interface IInvestmentOverviewController {
    InvestmentOverviewResponse getForUser(Long userId);
}
