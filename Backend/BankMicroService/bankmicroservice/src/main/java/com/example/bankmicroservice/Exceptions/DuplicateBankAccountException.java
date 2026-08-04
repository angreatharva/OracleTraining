package com.example.bankmicroservice.exceptions;

public class DuplicateBankAccountException extends RuntimeException {

    public DuplicateBankAccountException(String accountNumber) {
        super("A bank account already exists for account number ending in: " + mask(accountNumber));
    }

    private static String mask(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }
}
