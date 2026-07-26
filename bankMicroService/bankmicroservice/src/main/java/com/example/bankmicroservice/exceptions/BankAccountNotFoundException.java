package com.example.bankmicroservice.exceptions;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException(Long id) {
        super("Bank account not found with id: " + id);
    }
}
