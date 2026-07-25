package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.request.CreateTradeTransactionRequest;
import com.example.tradingmicroservice.dto.response.TradeTransactionResponse;
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

@RestController
@RequestMapping("/api/trade-transactions")
public class TradeTransactionController implements ITradeTransactionController {

    private final ITradeTransactionService transactionService;

    public TradeTransactionController(ITradeTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Override
    public ResponseEntity<TradeTransactionResponse> create(
            @Valid @RequestBody CreateTradeTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public TradeTransactionResponse getById(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @GetMapping
    @Override
    public List<TradeTransactionResponse> getAll(
            @RequestParam(required = false) Long portfolioAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        return transactionService.getAll(portfolioAccountId, status, type, startDate, endDate);
    }
}
