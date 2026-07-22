package com.example.wtms.Exceptions;

public class PortfolioStatementNotFoundException extends RuntimeException {
    public PortfolioStatementNotFoundException(Long id) {
        super("Portfolio Statement not found with id: " + id);
    }
}