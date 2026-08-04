package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.request.CreateKycDocumentRequest;
import com.example.bankmicroservice.dto.request.UpdateKycVerificationRequest;
import com.example.bankmicroservice.dto.response.KycDocumentResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IKycDocumentController {

    ResponseEntity<KycDocumentResponse> create(CreateKycDocumentRequest request);

    KycDocumentResponse getById(Long id);

    List<KycDocumentResponse> getAll(Long userId, String verificationStatus, String documentType);

    KycDocumentResponse updateVerification(Long id, UpdateKycVerificationRequest request);

    ResponseEntity<Void> deactivate(Long id);
}
