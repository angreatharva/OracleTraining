package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
import com.example.tradingmicroservice.enums.TransactionStatus;
import com.example.tradingmicroservice.security.AuthorizationHelper;
import com.example.tradingmicroservice.services.ITradeTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This controller is the authorization boundary for the whole trade saga.
 *
 * <p>Once {@code transactionService.create} starts, Trading talks to Product, Portfolio and
 * Bank using its SERVICE token, which bypasses their ownership checks. So the check that the
 * caller actually owns the portfolio account has to happen here, before the saga begins - it
 * cannot be recovered downstream.</p>
 */
@RestController
@RequestMapping("/api/trade-transactions")
public class TradeTransactionController implements ITradeTransactionController {

    private final ITradeTransactionService transactionService;
    private final AuthorizationHelper authorization;

    public TradeTransactionController(ITradeTransactionService transactionService,
                                      AuthorizationHelper authorization) {
        this.transactionService = transactionService;
        this.authorization = authorization;
    }

    @PostMapping
    @Override
    public ResponseEntity<TradeTransactionResponse> create(
            @Valid @RequestBody CreateTradeTransactionRequest request) {
        // Must precede the saga: everything after this point runs as a trusted service call.
        authorization.assertCanAccessPortfolioAccount(request.portfolioAccountId());

        TradeTransactionResponse response = transactionService.create(request);
        HttpStatus status = TransactionStatus.COMPLETED.name().equals(response.transactionStatus())
                ? HttpStatus.CREATED
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    @Override
    public TradeTransactionResponse getById(@PathVariable Long id) {
        TradeTransactionResponse trade = transactionService.getById(id);
        authorization.assertCanAccessPortfolioAccount(trade.portfolioAccountId());
        return trade;
    }

    @GetMapping
    @Override
    public List<TradeTransactionResponse> getAll(
            @RequestParam(required = false) Long portfolioAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        // Pinned to the caller's own account when they do not (or may not) name one.
        Long allowedAccount = authorization.restrictPortfolioAccountFilter(portfolioAccountId);
        return transactionService.getAll(allowedAccount, status, type, startDate, endDate);
    }
}
