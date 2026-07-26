package com.example.bankmicroservice.services;

import com.example.bankmicroservice.dto.request.CreateKycDocumentRequest;
import com.example.bankmicroservice.dto.request.UpdateKycVerificationRequest;
import com.example.bankmicroservice.dto.response.KycDocumentResponse;

import java.util.List;

public interface IKycDocumentService {

    KycDocumentResponse create(CreateKycDocumentRequest request);

    KycDocumentResponse getById(Long id);

    List<KycDocumentResponse> getAll(Long userId, String verificationStatus, String documentType);

    KycDocumentResponse updateVerification(Long id, UpdateKycVerificationRequest request);

    void deactivate(Long id);
}
