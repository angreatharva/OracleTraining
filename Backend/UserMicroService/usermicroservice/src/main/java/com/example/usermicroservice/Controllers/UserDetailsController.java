package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateUserDetailRequest;
import com.example.usermicroservice.dto.response.UserDetailResponse;
import com.example.usermicroservice.security.AuthorizationHelper;
import com.example.usermicroservice.service.IUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * A user-detail record is the risk/KYC profile of exactly one user, so every operation is
 * gated on that user: the owner may read it, their manager may read and maintain it.
 * Creating and deleting profiles is a manager (onboarding) responsibility.
 */
@RestController
@RequestMapping("/api/user-details")
public class UserDetailsController implements IUserDetailsController {
    private final IUserDetailsService userDetailService;
    private final AuthorizationHelper authorization;

    public UserDetailsController(IUserDetailsService userDetailService, AuthorizationHelper authorization) {
        this.userDetailService = userDetailService;
        this.authorization = authorization;
    }

    @PostMapping @Override
    public ResponseEntity<UserDetailResponse> create(@Valid @RequestBody CreateUserDetailRequest request) {
        authorization.assertManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(userDetailService.create(request));
    }

    @GetMapping @Override
    public List<UserDetailResponse> getAll() {
        authorization.assertManager();
        return userDetailService.getAll();
    }

    @GetMapping("/{id}") @Override
    public UserDetailResponse getById(@PathVariable Long id) {
        UserDetailResponse detail = userDetailService.getById(id);
        authorization.assertCanAccessUser(detail.userId());
        return detail;
    }

    @GetMapping("/user/{userId}") @Override
    public UserDetailResponse getByUserId(@PathVariable Long userId) {
        authorization.assertCanAccessUser(userId);
        return userDetailService.getByUserId(userId);
    }

    @PutMapping("/{id}") @Override
    public UserDetailResponse update(@PathVariable Long id, @Valid @RequestBody CreateUserDetailRequest request) {
        authorization.assertManager();
        return userDetailService.update(id, request);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Override
    public void delete(@PathVariable Long id) {
        authorization.assertManager();
        userDetailService.delete(id);
    }
}
