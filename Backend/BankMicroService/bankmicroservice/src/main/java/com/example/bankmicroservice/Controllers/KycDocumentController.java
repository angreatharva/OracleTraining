package com.example.bankmicroservice.controllers;

import com.example.bankmicroservice.dto.request.CreateKycDocumentRequest;
import com.example.bankmicroservice.dto.request.UpdateKycVerificationRequest;
import com.example.bankmicroservice.dto.response.KycDocumentResponse;
import com.example.bankmicroservice.security.AuthorizationHelper;
import com.example.bankmicroservice.services.IKycDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * An investor may submit and read their own KYC documents; only a MANAGER may decide whether
 * one is VERIFIED or REJECTED, which is the whole point of the verification workflow.
 */
@RestController
@RequestMapping("/api/kyc-documents")
public class KycDocumentController implements IKycDocumentController {

    private final IKycDocumentService kycDocumentService;
    private final AuthorizationHelper authorization;

    public KycDocumentController(IKycDocumentService kycDocumentService, AuthorizationHelper authorization) {
        this.kycDocumentService = kycDocumentService;
        this.authorization = authorization;
    }

    @PostMapping
    @Override
    public ResponseEntity<KycDocumentResponse> create(
            @Valid @RequestBody CreateKycDocumentRequest request) {
        authorization.assertCanAccessUser(request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(kycDocumentService.create(request));
    }

    @GetMapping("/{id}")
    @Override
    public KycDocumentResponse getById(@PathVariable Long id) {
        KycDocumentResponse document = kycDocumentService.getById(id);
        authorization.assertCanAccessUser(document.userId());
        return document;
    }

    @GetMapping
    @Override
    public List<KycDocumentResponse> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String documentType) {
        return kycDocumentService.getAll(
                authorization.restrictUserFilter(userId), verificationStatus, documentType);
    }

    @PutMapping("/{id}/verification")
    @Override
    public KycDocumentResponse updateVerification(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKycVerificationRequest request) {
        // An investor must not be able to approve their own KYC.
        authorization.assertManager();
        return kycDocumentService.updateVerification(id, request);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        authorization.assertManager();
        kycDocumentService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
