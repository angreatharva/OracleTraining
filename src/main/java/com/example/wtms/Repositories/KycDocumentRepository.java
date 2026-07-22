package com.example.wtms.Repositories;


import com.example.wtms.Entities.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KycDocumentRepository
        extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserId(Long userId);

    List<KycDocument> findByUserIdAndVerificationStatus(
            Long userId,
            String verificationStatus
    );

    Optional<KycDocument> findByDocumentNumber(String documentNumber);

    Optional<KycDocument> findByUserIdAndDocumentType(
            Long userId,
            String documentType
    );

    boolean existsByDocumentNumber(String documentNumber);
}