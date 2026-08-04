package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.ChangePasswordRequest;
import com.example.usermicroservice.dto.request.LoginRequest;
import com.example.usermicroservice.dto.response.LoginResponse;
import com.example.usermicroservice.dto.response.UserResponse;
import com.example.usermicroservice.security.CurrentUser;
import com.example.usermicroservice.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints. {@code /api/auth/login} is the only unauthenticated endpoint in
 * the whole system; everything else requires the token it issues.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;
    private final CurrentUser currentUser;

    public AuthController(IAuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Lets a client re-hydrate its session from a stored token without keeping user state. */
    @GetMapping("/me")
    public UserResponse me() {
        return authService.me(currentUser.require().userId());
    }

    /** Always operates on the caller's own account - a userId is deliberately not accepted. */
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser.require().userId(), request);
    }
}
