package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;
import com.example.tradingmicroservice.services.IPortfolioStatementService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio-statements")
public class PortfolioStatementController implements IPortfolioStatementController {

    private final IPortfolioStatementService statementService;

    public PortfolioStatementController(IPortfolioStatementService statementService) {
        this.statementService = statementService;
    }

    @PostMapping("/internal")
    @Override
    public ResponseEntity<PortfolioStatementResponse> create(
            @Valid @RequestBody CreatePortfolioStatementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statementService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public PortfolioStatementResponse getById(@PathVariable Long id) {
        return statementService.getById(id);
    }

    @GetMapping
    @Override
    public List<PortfolioStatementResponse> getAll(
            @RequestParam(required = false) Long portfolioAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return statementService.getAll(portfolioAccountId, status, startDate, endDate);
    }
}
