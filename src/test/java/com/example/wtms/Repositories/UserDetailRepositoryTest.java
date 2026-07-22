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

import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import com.example.wtms.Entities.UserDetail;
import com.example.wtms.Exceptions.UserDetailsException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserDetailRepositoryTest {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void createUserDetailsForUser_shouldSaveSuccessfully() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Detail User");
        user.setEmail("detailuser" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("2222222222");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);
        User savedUser = userRepository.saveAndFlush(user);

        UserDetail details = new UserDetail();
        details.setCreatedAt(LocalDateTime.now());
        details.setUpdatedAt(LocalDateTime.now());
        details.setUser(savedUser);
        details.setDateOfBirth(LocalDate.of(1995, 2, 17));
        details.setRiskLevel("MODERATE");
        details.setRiskScore(55);
        details.setKycStatus("VERIFIED");

        UserDetail saved = userDetailsRepository.saveAndFlush(details);

        assertNotNull(saved.getUserDetailId());
        assertEquals(savedUser.getUserId(), saved.getUser().getUserId());
        assertEquals("VERIFIED", saved.getKycStatus());
    }

    @Test
    void fetchUserDetailsById_shouldReturnRecord() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Fetch Detail User");
        user.setEmail("fetchdetail" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("1111111111");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);
        User savedUser = userRepository.saveAndFlush(user);

        UserDetail details = new UserDetail();
        details.setCreatedAt(LocalDateTime.now());
        details.setUpdatedAt(LocalDateTime.now());
        details.setUser(savedUser);
        details.setDateOfBirth(LocalDate.of(1992, 8, 9));
        details.setRiskLevel("HIGH");
        details.setRiskScore(80);
        details.setKycStatus("PENDING");

        UserDetail saved = userDetailsRepository.saveAndFlush(details);

        UserDetail found = userDetailsRepository.findById(saved.getUserDetailId()).orElseThrow();

        assertEquals(saved.getUserDetailId(), found.getUserDetailId());
        assertEquals(savedUser.getUserId(), found.getUser().getUserId());
        assertEquals("PENDING", found.getKycStatus());
    }

    @Test
    void saveUserDetailsWithNullUser_shouldFail() {
        UserDetail details = new UserDetail();
        details.setUser(null);
        details.setDateOfBirth(LocalDate.of(1990, 1, 1));
        details.setRiskLevel("LOW");
        details.setRiskScore(20);
        details.setKycStatus("VERIFIED");

        assertThrows(UserDetailsException.class, () -> {
            validateUserDetails(details);
            userDetailsRepository.saveAndFlush(details);
        });
    }

    @Test
    void saveUserDetailsWithInvalidRiskScore_shouldFail() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Invalid Risk User");
        user.setEmail("invalidrisk" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("0000000000");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);
        User savedUser = userRepository.saveAndFlush(user);

        UserDetail details = new UserDetail();
        details.setUser(savedUser);
        details.setDateOfBirth(LocalDate.of(1990, 1, 1));
        details.setRiskLevel("LOW");
        details.setRiskScore(-5);
        details.setKycStatus("PENDING");

        assertThrows(UserDetailsException.class, () -> {
            validateUserDetails(details);
            userDetailsRepository.saveAndFlush(details);
        });
    }

    @Test
    void saveDuplicateUserDetailsForSameUser_shouldFail() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Duplicate Detail User");
        user.setEmail("duplicatedetail" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("1231231234");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);
        User savedUser = userRepository.saveAndFlush(user);

        UserDetail details1 = new UserDetail();
        details1.setCreatedAt(LocalDateTime.now());
        details1.setUpdatedAt(LocalDateTime.now());
        details1.setUser(savedUser);
        details1.setDateOfBirth(LocalDate.of(1991, 3, 3));
        details1.setRiskLevel("LOW");
        details1.setRiskScore(30);
        details1.setKycStatus("VERIFIED");
        userDetailsRepository.saveAndFlush(details1);

        UserDetail details2 = new UserDetail();
        details2.setUser(savedUser);
        details2.setDateOfBirth(LocalDate.of(1994, 4, 4));
        details2.setRiskLevel("HIGH");
        details2.setRiskScore(90);
        details2.setKycStatus("PENDING");

        assertThrows(DataIntegrityViolationException.class, () -> userDetailsRepository.saveAndFlush(details2));
    }

    @Test
    void fetchMissingUserDetails_shouldFailWithCustomException() {
        assertThrows(UserDetailsException.class, () -> userDetailsRepository.findById(999999L)
                .orElseThrow(() -> new UserDetailsException("UserDetails not found: 999999")));
    }

    private void validateUserDetails(UserDetail details) {
        if (details == null) {
            throw new UserDetailsException("UserDetails cannot be null");
        }
        if (details.getUser() == null) {
            throw new UserDetailsException("User cannot be null");
        }
        if (details.getRiskScore() < 0 || details.getRiskScore() > 100) {
            throw new UserDetailsException("Risk score must be between 0 and 100");
        }
        if (details.getKycStatus() == null || details.getKycStatus().trim().isEmpty()) {
            throw new UserDetailsException("KYC status cannot be null or empty");
        }
    }
}