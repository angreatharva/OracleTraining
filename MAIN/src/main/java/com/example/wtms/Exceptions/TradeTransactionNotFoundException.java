package com.example.wtms.Exceptions;

public class TradeTransactionNotFoundException extends RuntimeException {
    public TradeTransactionNotFoundException(Long id) {
        super("Trade Transaction not found with id: " + id);
    }
}

