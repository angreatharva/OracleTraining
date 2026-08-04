package com.example.usermicroservice.service;

import com.example.commonsecurity.AuthenticatedUser;
import com.example.commonsecurity.JwtProperties;
import com.example.commonsecurity.JwtService;
import com.example.usermicroservice.dto.request.ChangePasswordRequest;
import com.example.usermicroservice.dto.request.LoginRequest;
import com.example.usermicroservice.dto.response.LoginResponse;
import com.example.usermicroservice.dto.response.UserResponse;
import com.example.usermicroservice.entities.User;
import com.example.usermicroservice.enums.UserStatus;
import com.example.usermicroservice.exceptions.InvalidCredentialsException;
import com.example.usermicroservice.exceptions.ResourceNotFoundException;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // A suspended or inactive user must not receive a token, but the failure is reported
        // identically to a wrong password so the endpoint cannot be used to probe accounts.
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new InvalidCredentialsException();
        }

        AuthenticatedUser identity = toIdentity(user);
        String token = jwtService.generate(identity);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                java.time.Instant.now().plus(jwtProperties.getExpiry()), ZoneId.systemDefault());

        return new LoginResponse(token, expiresAt, identity.roleName(), toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return toResponse(getUser(userId));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AuthenticatedUser toIdentity(User user) {
        return new AuthenticatedUser(
                user.getUserId(),
                user.getRole().getRoleId(),
                user.getRole().getRoleName(),
                user.getManager() == null ? null : user.getManager().getUserId(),
                user.getEmail());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getRole().getRoleId(),
                user.getManager() == null ? null : user.getManager().getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                UserStatus.valueOf(user.getStatus()),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
