package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.response.InvestmentOverviewResponse;
import com.example.tradingmicroservice.security.AuthorizationHelper;
import com.example.tradingmicroservice.services.InvestmentOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investment-overview")
public class InvestmentOverviewController implements IInvestmentOverviewController {

    private final InvestmentOverviewService investmentOverviewService;
    private final AuthorizationHelper authorization;

    public InvestmentOverviewController(InvestmentOverviewService investmentOverviewService,
                                        AuthorizationHelper authorization) {
        this.investmentOverviewService = investmentOverviewService;
        this.authorization = authorization;
    }

    /** Keyed by userId directly, so the standard self-or-my-manager rule applies as-is. */
    @Override
    @GetMapping("/users/{userId}")
    public InvestmentOverviewResponse getForUser(@PathVariable Long userId) {
        authorization.assertCanAccessUser(userId);
        return investmentOverviewService.getForUser(userId);
    }
}
