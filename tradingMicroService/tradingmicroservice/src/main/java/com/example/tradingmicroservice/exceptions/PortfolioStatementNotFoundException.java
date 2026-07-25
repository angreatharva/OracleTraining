package com.example.tradingmicroservice.exceptions;

public class PortfolioStatementNotFoundException extends RuntimeException {

    public PortfolioStatementNotFoundException(Long id) {
        super("Portfolio statement not found with id: " + id);
    }
}
