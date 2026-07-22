package com.example.wtms.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import com.example.wtms.Exceptions.UserException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void createUserWithRole_shouldSaveSuccessfully() {
        Role role = new Role();
        role.setRoleName("INVESTOR_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Test User");
        user.setEmail("testuser" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("9999999999");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        User saved = userRepository.saveAndFlush(user);

        assertNotNull(saved.getUserId());
        assertEquals(user.getFullName(), saved.getFullName());
        assertEquals(savedRole.getRoleId(), saved.getRole().getRoleId());
    }

    @Test
    void fetchUserById_shouldReturnUser() {
        Role role = new Role();
        role.setRoleName("MANAGER_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Fetch User");
        user.setEmail("fetchuser" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("8888888888");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        User saved = userRepository.saveAndFlush(user);

        User found = userRepository.findById(saved.getUserId()).orElseThrow();

        assertEquals(saved.getUserId(), found.getUserId());
        assertEquals(saved.getEmail(), found.getEmail());
        assertEquals(savedRole.getRoleId(), found.getRole().getRoleId());
    }

    @Test
    void saveUserWithNullEmail_shouldFail() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setFullName("Null Email User");
        user.setEmail(null);
        user.setPasswordHash("secret");
        user.setPhone("7777777777");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        assertThrows(UserException.class, () -> {
            validateUser(user);
            userRepository.saveAndFlush(user);
        });
    }

    @Test
    void saveUserWithEmptyEmail_shouldFail() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setFullName("Empty Email User");
        user.setEmail("   ");
        user.setPasswordHash("secret");
        user.setPhone("6666666666");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        assertThrows(UserException.class, () -> {
            validateUser(user);
            userRepository.saveAndFlush(user);
        });
    }

    @Test
    void saveDuplicateEmail_shouldFail() {
        Role role = new Role();
        role.setRoleName("ROLE_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        String email = "duplicate" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com";

        User user1 = new User();
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setFullName("User One");
        user1.setEmail(email);
        user1.setPasswordHash("secret");
        user1.setPhone("5555555555");
        user1.setStatus("ACTIVE");
        user1.setRole(savedRole);
        userRepository.saveAndFlush(user1);

        User user2 = new User();
        user2.setFullName("User Two");
        user2.setEmail(email);
        user2.setPasswordHash("secret");
        user2.setPhone("4444444444");
        user2.setStatus("ACTIVE");
        user2.setRole(savedRole);

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    void fetchMissingUser_shouldFailWithCustomException() {
        assertThrows(UserException.class, () -> userRepository.findById(999999L)
                .orElseThrow(() -> new UserException("User not found: 999999")));
    }

    @Test
    void assignExistingUserToExistingRole_shouldWork() {
        Role role = new Role();
        role.setRoleName("LINK_" + UUID.randomUUID().toString().substring(0, 8));
        Role savedRole = roleRepository.saveAndFlush(role);

        User user = new User();
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFullName("Linked User");
        user.setEmail("linked" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
        user.setPasswordHash("secret");
        user.setPhone("3333333333");
        user.setStatus("ACTIVE");
        user.setRole(savedRole);

        User saved = userRepository.saveAndFlush(user);

        assertNotNull(saved.getRole());
        assertEquals(savedRole.getRoleId(), saved.getRole().getRoleId());
    }

    private void validateUser(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new UserException("User email cannot be null or empty");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new UserException("User name cannot be null or empty");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new UserException("Password hash cannot be null or empty");
        }
        if (user.getRole() == null) {
            throw new UserException("Role cannot be null");
        }
    }
}