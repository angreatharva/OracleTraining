package com.example.usermicroservice.service;

import com.example.usermicroservice.clients.BankRecordClient;
import com.example.usermicroservice.dto.request.CreateUserRequest;
import com.example.usermicroservice.dto.response.UserResponse;
import com.example.usermicroservice.entities.Role;
import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.enums.UserStatus;
import com.example.usermicroservice.exceptions.ResourceNotFoundException;
import com.example.usermicroservice.exceptions.UserDeletionBlockedException;
import com.example.usermicroservice.repositories.RoleRepository;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BankRecordClient bankRecordClient;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       BankRecordClient bankRecordClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bankRecordClient = bankRecordClient;
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) throw new IllegalArgumentException("Email already exists: " + request.email());
        return toResponse(userRepository.save(toEntity(request, new User())));
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
    public UserResponse update(Long id, CreateUserRequest request) {
        User user = getEntityById(id);
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }
        return toResponse(userRepository.save(toEntity(request, user)));
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

    private User toEntity(CreateUserRequest request, User user) {
        user.setRole(getRole(request.roleId()));
        user.setManager(request.managerId() == null ? null : getEntityById(request.managerId()));
        user.setPasswordHash(request.passwordHash());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setStatus((request.status() == null ? UserStatus.ACTIVE : request.status()).name());
        return user;
    }

    private Role getRole(Long id) { return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", id)); }
    private User getEntityById(Long id) { return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id)); }
    private UserResponse toResponse(User user) {
        return new UserResponse(user.getUserId(), user.getRole().getRoleId(), user.getManager() == null ? null : user.getManager().getUserId(),
                user.getEmail(), user.getFullName(), user.getPhone(), UserStatus.valueOf(user.getStatus()), user.getCreatedAt(), user.getUpdatedAt());
    }
}
