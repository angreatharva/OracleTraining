package com.example.bankmicroservice.exceptions;

public class DuplicateKycDocumentException extends RuntimeException {

    public DuplicateKycDocumentException(Long userId, String documentType) {
        super("An active " + documentType + " document already exists for user: " + userId);
    }
}
