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
import com.example.bankmicroservice.exceptions.DuplicateKycDocumentException;
import com.example.bankmicroservice.exceptions.KycDocumentNotFoundException;
import com.example.bankmicroservice.exceptions.UserValidationException;
import com.example.bankmicroservice.repositories.KycDocumentRepository;
import com.example.bankmicroservice.utils.EnumParser;
import com.example.bankmicroservice.utils.MaskingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@Transactional
public class KycDocumentService implements IKycDocumentService {

    private final KycDocumentRepository kycDocumentRepository;
    private final UserServiceClient userServiceClient;

    public KycDocumentService(
            KycDocumentRepository kycDocumentRepository,
            UserServiceClient userServiceClient) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public KycDocumentResponse create(CreateKycDocumentRequest request) {
        validateUser(request.userId());
        DocumentType documentType = EnumParser.parse(
                request.documentType(), DocumentType.class, "documentType");
        String normalizedNumber = normalize(request.documentNumber());

        boolean duplicate = kycDocumentRepository
                .existsByUserIdAndDocumentTypeAndDocumentNumberAndStatus(
                        request.userId(), documentType, normalizedNumber, RecordStatus.ACTIVE);
        if (duplicate) {
            throw new DuplicateKycDocumentException(request.userId(), documentType.name());
        }

        KycDocument document = KycDocument.builder()
                .userId(request.userId())
                .documentType(documentType)
                .documentNumber(normalizedNumber)
                .fileName(trimToNull(request.fileName()))
                .verificationStatus(VerificationStatus.PENDING)
                .submittedDate(LocalDate.now())
                .status(RecordStatus.ACTIVE)
                .build();

        return toResponse(kycDocumentRepository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public KycDocumentResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> getAll(Long userId, String verificationStatus, String documentType) {
        VerificationStatus parsedVerificationStatus = verificationStatus == null
                ? null
                : EnumParser.parse(verificationStatus, VerificationStatus.class, "verificationStatus");
        DocumentType parsedDocumentType = documentType == null
                ? null
                : EnumParser.parse(documentType, DocumentType.class, "documentType");

        Stream<KycDocument> documents = userId == null
                ? kycDocumentRepository.findAll().stream()
                : kycDocumentRepository.findByUserId(userId).stream();

        return documents
                .filter(document -> parsedVerificationStatus == null ||
                        document.getVerificationStatus() == parsedVerificationStatus)
                .filter(document -> parsedDocumentType == null || document.getDocumentType() == parsedDocumentType)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public KycDocumentResponse updateVerification(Long id, UpdateKycVerificationRequest request) {
        KycDocument document = getEntityById(id);
        VerificationStatus newStatus = EnumParser.parse(
                request.verificationStatus(), VerificationStatus.class, "verificationStatus");
        document.setVerificationStatus(newStatus);
        document.setVerifiedDate(newStatus == VerificationStatus.VERIFIED ? LocalDate.now() : null);
        return toResponse(kycDocumentRepository.save(document));
    }

    @Override
    public void deactivate(Long id) {
        KycDocument document = getEntityById(id);
        document.setStatus(RecordStatus.INACTIVE);
        kycDocumentRepository.save(document);
    }

    private KycDocument getEntityById(Long id) {
        return kycDocumentRepository.findById(id)
                .orElseThrow(() -> new KycDocumentNotFoundException(id));
    }

    private void validateUser(Long userId) {
        UserValidationResult validation = userServiceClient.validateUser(userId);
        if (validation == null || !validation.usable()) {
            String reason = validation == null ? "User service returned no result" : validation.message();
            throw new UserValidationException(userId, reason);
        }
    }

    private KycDocumentResponse toResponse(KycDocument document) {
        return new KycDocumentResponse(
                document.getKycDocumentId(),
                document.getUserId(),
                document.getDocumentType().name(),
                MaskingUtils.mask(document.getDocumentNumber()),
                document.getFileName(),
                document.getVerificationStatus().name(),
                document.getSubmittedDate(),
                document.getVerifiedDate(),
                document.getStatus().name(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
