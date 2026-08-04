package com.example.bankmicroservice.repositories;

import com.example.bankmicroservice.entities.KycDocument;
import com.example.bankmicroservice.enums.DocumentType;
import com.example.bankmicroservice.enums.RecordStatus;
import com.example.bankmicroservice.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycDocumentRepository
        extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserId(Long userId);

    List<KycDocument> findByStatus(RecordStatus status);

    List<KycDocument> findByVerificationStatus(
            VerificationStatus verificationStatus
    );

    List<KycDocument> findByDocumentType(DocumentType documentType);

    List<KycDocument> findByUserIdAndDocumentType(
            Long userId,
            DocumentType documentType
    );

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByUserIdAndDocumentType(
            Long userId,
            DocumentType documentType
    );

    boolean existsByUserIdAndDocumentTypeAndDocumentNumberAndStatus(
            Long userId,
            DocumentType documentType,
            String documentNumber,
            RecordStatus status
    );
}