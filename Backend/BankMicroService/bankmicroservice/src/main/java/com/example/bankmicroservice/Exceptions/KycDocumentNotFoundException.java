package com.example.bankmicroservice.exceptions;

public class KycDocumentNotFoundException extends RuntimeException {

    public KycDocumentNotFoundException(Long id) {
        super("KYC document not found with id: " + id);
    }
}
