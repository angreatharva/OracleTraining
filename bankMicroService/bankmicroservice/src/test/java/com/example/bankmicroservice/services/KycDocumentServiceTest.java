package com.example.bankmicroservice.services;

import com.example.bankmicroservice.clients.UserServiceClient;
import com.example.bankmicroservice.clients.model.UserValidationResult;
import com.example.bankmicroservice.dto.request.CreateKycDocumentRequest;
import com.example.bankmicroservice.dto.request.UpdateKycVerificationRequest;
import com.example.bankmicroservice.dto.response.KycDocumentResponse;
import com.example.bankmicroservice.entities.KycDocument;
import com.example.bankmicroservice.enums.DocumentType;
import com.example.bankmicroservice.enums.RecordStatus;
import com.example.bankmicroservice.enums.VerificationStatus;
import com.example.bankmicroservice.repositories.KycDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycDocumentServiceTest {

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private UserServiceClient userServiceClient;

    private KycDocumentService service;

    @BeforeEach
    void setUp() {
        service = new KycDocumentService(kycDocumentRepository, userServiceClient);
    }

    @Test
    void createStartsDocumentInPendingState() {
        when(userServiceClient.validateUser(5L))
                .thenReturn(new UserValidationResult(5L, true, true, "ok"));
        when(kycDocumentRepository.existsByUserIdAndDocumentTypeAndDocumentNumberAndStatus(
                5L, DocumentType.PAN, "ABCDE1234F", RecordStatus.ACTIVE)).thenReturn(false);
        when(kycDocumentRepository.save(any(KycDocument.class))).thenAnswer(invocation -> {
            KycDocument document = invocation.getArgument(0);
            document.setKycDocumentId(9L);
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());
            return document;
        });

        KycDocumentResponse response = service.create(
                new CreateKycDocumentRequest(5L, "pan", "ABCDE 1234 F", "pan.pdf")
        );

        assertThat(response.kycDocumentId()).isEqualTo(9L);
        assertThat(response.verificationStatus()).isEqualTo("PENDING");
        assertThat(response.maskedDocumentNumber()).endsWith("234F");
    }

    @Test
    void verifySetsVerifiedDate() {
        KycDocument document = KycDocument.builder()
                .kycDocumentId(9L)
                .userId(5L)
                .documentType(DocumentType.PAN)
                .documentNumber("ABCDE1234F")
                .verificationStatus(VerificationStatus.PENDING)
                .submittedDate(LocalDate.now())
                .status(RecordStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(kycDocumentRepository.findById(9L)).thenReturn(Optional.of(document));
        when(kycDocumentRepository.save(document)).thenReturn(document);

        KycDocumentResponse response = service.updateVerification(
                9L,
                new UpdateKycVerificationRequest("verified")
        );

        assertThat(response.verificationStatus()).isEqualTo("VERIFIED");
        assertThat(response.verifiedDate()).isEqualTo(LocalDate.now());
    }
}
