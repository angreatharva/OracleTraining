package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.response.InvestmentOverviewResponse;
import com.example.tradingmicroservice.services.InvestmentOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investment-overview")
public class InvestmentOverviewController implements IInvestmentOverviewController {

    private final InvestmentOverviewService investmentOverviewService;

    public InvestmentOverviewController(InvestmentOverviewService investmentOverviewService) {
        this.investmentOverviewService = investmentOverviewService;
    }

    @Override
    @GetMapping("/users/{userId}")
    public InvestmentOverviewResponse getForUser(@PathVariable Long userId) {
        return investmentOverviewService.getForUser(userId);
    }
}
