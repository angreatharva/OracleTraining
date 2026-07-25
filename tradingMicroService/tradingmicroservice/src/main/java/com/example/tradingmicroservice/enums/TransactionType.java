package com.example.tradingmicroservice.enums;

public enum TransactionType {
    BUY,
    SELL;

    public static TransactionType from(String value) {
        try {
            return TransactionType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("transactionType must be BUY or SELL");
        }
    }
}
