package com.example.tradingmicroservice.exceptions;

public class TradeTransactionNotFoundException extends RuntimeException {

    public TradeTransactionNotFoundException(Long id) {
        super("Trade transaction not found with id: " + id);
    }
}
