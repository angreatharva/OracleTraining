package com.example.wtms.Repositories;

import com.example.wtms.Entities.KycDocument;
import com.example.wtms.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUser(User user);

    List<KycDocument> findByUser_UserId(Long userId);

    List<KycDocument> findByStatus(String status);

    List<KycDocument> findByVerificationStatus(String verificationStatus);

    List<KycDocument> findByDocumentType(String documentType);

    List<KycDocument> findByUser_UserIdAndDocumentType(Long userId, String documentType);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByUser_UserIdAndDocumentType(Long userId, String documentType);
}