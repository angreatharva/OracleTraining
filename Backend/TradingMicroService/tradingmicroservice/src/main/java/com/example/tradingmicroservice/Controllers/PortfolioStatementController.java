package com.example.tradingmicroservice.controllers;

import com.example.tradingmicroservice.dto.request.CreatePortfolioStatementRequest;
import com.example.tradingmicroservice.dto.response.PortfolioStatementResponse;
import com.example.tradingmicroservice.security.AuthorizationHelper;
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
    private final AuthorizationHelper authorization;

    public PortfolioStatementController(IPortfolioStatementService statementService,
                                        AuthorizationHelper authorization) {
        this.statementService = statementService;
        this.authorization = authorization;
    }

    /**
     * Statement creation takes opening/closing values straight from the request body, so
     * letting an end user call it would let them author their own statement figures.
     * SERVICE-only, matching the {@code /internal} path it already advertises.
     */
    @PostMapping("/internal")
    @Override
    public ResponseEntity<PortfolioStatementResponse> create(
            @Valid @RequestBody CreatePortfolioStatementRequest request) {
        authorization.assertServiceCall();
        return ResponseEntity.status(HttpStatus.CREATED).body(statementService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public PortfolioStatementResponse getById(@PathVariable Long id) {
        PortfolioStatementResponse statement = statementService.getById(id);
        authorization.assertCanAccessPortfolioAccount(statement.portfolioAccountId());
        return statement;
    }

    @GetMapping
    @Override
    public List<PortfolioStatementResponse> getAll(
            @RequestParam(required = false) Long portfolioAccountId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Long allowedAccount = authorization.restrictPortfolioAccountFilter(portfolioAccountId);
        return statementService.getAll(allowedAccount, status, startDate, endDate);
    }
}
