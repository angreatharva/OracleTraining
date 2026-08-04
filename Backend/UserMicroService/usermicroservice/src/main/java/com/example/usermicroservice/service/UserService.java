package com.example.usermicroservice.service;

import com.example.usermicroservice.clients.BankRecordClient;
import com.example.usermicroservice.dto.request.CreateUserRequest;
import com.example.usermicroservice.dto.request.UpdateUserRequest;
import com.example.usermicroservice.dto.response.UserResponse;
import com.example.usermicroservice.entities.Role;
import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.enums.UserStatus;
import com.example.usermicroservice.exceptions.ResourceNotFoundException;
import com.example.usermicroservice.exceptions.UserDeletionBlockedException;
import com.example.usermicroservice.repositories.RoleRepository;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BankRecordClient bankRecordClient;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       BankRecordClient bankRecordClient, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bankRecordClient = bankRecordClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) throw new IllegalArgumentException("Email already exists: " + request.email());
        User user = new User();
        applyProfile(user, request.roleId(), request.managerId(), request.email(),
                request.fullName(), request.phone(), request.status());
        // The only place a plaintext password enters the system; it is never stored as-is.
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    @Override @Transactional(readOnly = true)
    public UserResponse getById(Long id) { return toResponse(getEntityById(id)); }

    @Override @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return toResponse(userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", email)));
    }

    @Override @Transactional(readOnly = true)
    public List<UserResponse> getAll() { return userRepository.findAll().stream().map(this::toResponse).toList(); }

    @Override @Transactional(readOnly = true)
    public List<UserResponse> getByRoleId(Long roleId) { return userRepository.findByRole_RoleId(roleId).stream().map(this::toResponse).toList(); }

    @Override @Transactional(readOnly = true)
    public List<UserResponse> getByManagerId(Long managerId) { return userRepository.findByManager_UserId(managerId).stream().map(this::toResponse).toList(); }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getEntityById(id);
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }
        if (id.equals(request.managerId())) {
            throw new IllegalArgumentException("A user cannot be their own manager");
        }
        applyProfile(user, request.roleId(), request.managerId(), request.email(),
                request.fullName(), request.phone(), request.status());
        // Password is intentionally untouched here - see POST /api/auth/change-password.
        return toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        User user = getEntityById(id);
        if (!user.getSubordinates().isEmpty()) throw new IllegalStateException("User manages one or more users");
        if (bankRecordClient.hasBankAccountOrKycDocument(id)) {
            throw new UserDeletionBlockedException(id);
        }
        userRepository.delete(user);
    }

    /** Shared by create and update; deliberately excludes the password. */
    private void applyProfile(User user, Long roleId, Long managerId, String email,
                              String fullName, String phone, UserStatus status) {
        user.setRole(getRole(roleId));
        user.setManager(managerId == null ? null : getEntityById(managerId));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setStatus((status == null ? UserStatus.ACTIVE : status).name());
    }

    private Role getRole(Long id) { return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", id)); }
    private User getEntityById(Long id) { return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id)); }
    private UserResponse toResponse(User user) {
        return new UserResponse(user.getUserId(), user.getRole().getRoleId(), user.getManager() == null ? null : user.getManager().getUserId(),
                user.getEmail(), user.getFullName(), user.getPhone(), UserStatus.valueOf(user.getStatus()), user.getCreatedAt(), user.getUpdatedAt());
    }
}
