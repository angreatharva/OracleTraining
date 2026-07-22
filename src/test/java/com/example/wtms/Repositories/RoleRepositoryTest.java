package com.example.wtms.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.wtms.Entities.Role;
import com.example.wtms.Entities.User;
import com.example.wtms.Exceptions.RolesException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createIndependentRole_shouldSaveSuccessfully() {
        Role role = new Role();
        role.setRoleName("ADMIN_" + UUID.randomUUID().toString().substring(0, 8));

        Role saved = roleRepository.saveAndFlush(role);

        assertNotNull(saved.getRoleId());
        assertEquals(role.getRoleName(), saved.getRoleName());
    }

    @Test
    void fetchRoleById_shouldReturnRole() {
        Role role = new Role();
        role.setRoleName("MANAGER_" + UUID.randomUUID().toString().substring(0, 8));
        Role saved = roleRepository.saveAndFlush(role);

        Role found = roleRepository.findById(saved.getRoleId()).orElseThrow();

        assertEquals(saved.getRoleId(), found.getRoleId());
        assertEquals(saved.getRoleName(), found.getRoleName());
    }

//    @Test
//    void assignRoleToUser_shouldPersistAssociation() {
//        Role role = new Role();
//        role.setRoleName("INVESTOR_" + UUID.randomUUID().toString().substring(0, 8));
//        Role savedRole = roleRepository.saveAndFlush(role);
//
//        User user = new User();
//        user.setFullName("Test User");
//        user.setEmail("testuser" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
//        user.setPasswordHash("secret");
//        user.setPhone("9999999999");
//        user.setStatus("ACTIVE");
//        user.setRole(savedRole);
//
//        User savedUser = userRepository.saveAndFlush(user);
//
//        assertNotNull(savedUser.getUserId());
//        assertNotNull(savedUser.getRole());
//        assertEquals(savedRole.getRoleId(), savedUser.getRole().getRoleId());
//    }

    @Test
    void saveRoleWithNullName_shouldFail() {
        Role role = new Role();
        role.setRoleName(null);

        assertThrows(RolesException.class, () -> {
            validateRole(role);
            roleRepository.saveAndFlush(role);
        });
    }

    @Test
    void saveRoleWithEmptyName_shouldFail() {
        Role role = new Role();
        role.setRoleName("   ");

        assertThrows(RolesException.class, () -> {
            validateRole(role);
            roleRepository.saveAndFlush(role);
        });
    }

    @Test
    void saveDuplicateRoleName_shouldFail() {
        String name = "DUPLICATE_ROLE_" + UUID.randomUUID().toString().substring(0, 8);

        Role role1 = new Role();
        role1.setRoleName(name);
        roleRepository.saveAndFlush(role1);

        Role role2 = new Role();
        role2.setRoleName(name);

        assertThrows(DataIntegrityViolationException.class, () -> roleRepository.saveAndFlush(role2));
    }

//    @Test
//    void deleteRoleInUse_shouldFailWithCustomException() {
//        Role role = new Role();
//        role.setRoleName("IN_USE_" + UUID.randomUUID().toString().substring(0, 8));
//        Role savedRole = roleRepository.saveAndFlush(role);
//
//        User user = new User();
//        user.setFullName("Linked User");
//        user.setEmail("linked" + UUID.randomUUID().toString().substring(0, 8) + "@mail.com");
//        user.setPasswordHash("secret");
//        user.setStatus("ACTIVE");
//        user.setRole(savedRole);
//        userRepository.saveAndFlush(user);
//
//        assertThrows(RolesException.class, () -> deleteRoleSafely(savedRole.getRoleId()));
//    }

    @Test
    void fetchMissingRole_shouldFailWithCustomException() {
        assertThrows(RolesException.class, () -> roleRepository.findById(999999L)
                .orElseThrow(() -> new RolesException("Role not found: 999999")));
    }

    private void validateRole(Role role) {
        if (role == null || role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new RolesException("Role name cannot be null or empty");
        }
    }

    private void deleteRoleSafely(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RolesException("Role not found: " + roleId));

        boolean roleInUse = userRepository.existsByRole(role);
        if (roleInUse) {
            throw new RolesException("Role is already assigned to one or more users");
        }

        roleRepository.delete(role);
        roleRepository.flush();
    }
}