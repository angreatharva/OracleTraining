package com.example.wtms.Exceptions;

public class InvalidTransactionStatusException extends RuntimeException {
    public InvalidTransactionStatusException(String status) {
        super("Invalid transaction status: " + status);
    }
}