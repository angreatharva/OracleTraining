package com.example.wtms.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.wtms.Entities.KycDocument;
import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KycDocumentRepositoryTest {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @Rollback(false)
    void createKycDocument_shouldSaveSuccessfully() {
        User user = createTestUser();

        KycDocument document = new KycDocument();
        document.setUser(user);
        document.setDocumentType("PAN");
        document.setDocumentNumber("ABCDE1234F");
        document.setFileName("pan.pdf");
        document.setVerificationStatus("PENDING");
        document.setSubmittedDate(LocalDate.now());
        document.setStatus("ACTIVE");

        KycDocument saved = kycDocumentRepository.saveAndFlush(document);

        assertNotNull(saved.getKycDocumentId());
        assertEquals("PAN", saved.getDocumentType());
        assertEquals(user.getUserId(), saved.getUser().getUserId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertNotNull(saved.getSubmittedDate());
    }

    @Test
    void fetchKycDocumentById_shouldReturnDocument() {
        User user = createTestUser();

        KycDocument document = new KycDocument();
        document.setUser(user);
        document.setDocumentType("AADHAAR");
        document.setDocumentNumber("123456789012");
        document.setFileName("aadhaar.pdf");
        document.setVerificationStatus("PENDING");
        document.setSubmittedDate(LocalDate.now());
        document.setStatus("ACTIVE");

        KycDocument saved = kycDocumentRepository.saveAndFlush(document);

        KycDocument found = kycDocumentRepository.findById(saved.getKycDocumentId()).orElseThrow();

        assertEquals(saved.getKycDocumentId(), found.getKycDocumentId());
        assertEquals(saved.getDocumentType(), found.getDocumentType());
        assertEquals(saved.getUser().getUserId(), found.getUser().getUserId());
    }

    @Test
    void findByUserId_shouldReturnAllKycDocumentsOfUser() {
        User user = createTestUser();

        KycDocument doc1 = new KycDocument();
        doc1.setUser(user);
        doc1.setDocumentType("PAN");
        doc1.setDocumentNumber("ABCDE1234F");
        doc1.setVerificationStatus("PENDING");
        doc1.setSubmittedDate(LocalDate.now());
        doc1.setStatus("ACTIVE");
        kycDocumentRepository.saveAndFlush(doc1);

        KycDocument doc2 = new KycDocument();
        doc2.setUser(user);
        doc2.setDocumentType("AADHAAR");
        doc2.setDocumentNumber("123456789012");
        doc2.setVerificationStatus("PENDING");
        doc2.setSubmittedDate(LocalDate.now());
        doc2.setStatus("ACTIVE");
        kycDocumentRepository.saveAndFlush(doc2);

        assertEquals(2, kycDocumentRepository.findByUser_UserId(user.getUserId()).size());
    }

    @Test
    void saveKycDocumentWithNullDocumentType_shouldFail() {
        User user = createTestUser();

        KycDocument document = new KycDocument();
        document.setUser(user);
        document.setDocumentType(null);
        document.setDocumentNumber("ABCDE1234F");
        document.setVerificationStatus("PENDING");
        document.setSubmittedDate(LocalDate.now());
        document.setStatus("ACTIVE");

        assertThrows(DataIntegrityViolationException.class, () -> kycDocumentRepository.saveAndFlush(document));
    }

    @Test
    void saveKycDocumentWithoutUser_shouldFail() {
        KycDocument document = new KycDocument();
        document.setDocumentType("PAN");
        document.setDocumentNumber("ABCDE1234F");
        document.setVerificationStatus("PENDING");
        document.setSubmittedDate(LocalDate.now());
        document.setStatus("ACTIVE");

        assertThrows(DataIntegrityViolationException.class, () -> kycDocumentRepository.saveAndFlush(document));
    }

    private User createTestUser() {
        Role role = new Role();
        role.setRoleName("KYC_ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("KYC Test User");
        user.setEmail("kycuser" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("8888888888");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        return userRepository.saveAndFlush(user);
    }
}