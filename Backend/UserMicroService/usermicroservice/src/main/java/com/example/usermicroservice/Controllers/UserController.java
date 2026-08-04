package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateUserRequest;
import com.example.usermicroservice.dto.request.UpdateUserRequest;
import com.example.usermicroservice.dto.response.UserResponse;
import com.example.usermicroservice.security.AuthorizationHelper;
import com.example.usermicroservice.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authorization rules applied here:
 * <ul>
 *   <li>anything that creates, edits, deletes or enumerates users is MANAGER-only;</li>
 *   <li>reading a single user is allowed for that user themselves, or for their manager.</li>
 * </ul>
 * The single-user read stays open to self because Bank and Portfolio call it (carrying the
 * end user's own token) to validate an owner before creating an account.
 */
@RestController
@RequestMapping("/api/users")
public class UserController implements IUserController {
    private final IUserService userService;
    private final AuthorizationHelper authorization;

    public UserController(IUserService userService, AuthorizationHelper authorization) {
        this.userService = userService;
        this.authorization = authorization;
    }

    @PostMapping @Override
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        authorization.assertManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping @Override
    public List<UserResponse> getAll() {
        authorization.assertManager();
        return userService.getAll();
    }

    @GetMapping("/{id}") @Override
    public UserResponse getById(@PathVariable Long id) {
        authorization.assertCanAccessUser(id);
        return userService.getById(id);
    }

    @GetMapping("/email/{email}") @Override
    public UserResponse getByEmail(@PathVariable String email) {
        UserResponse user = userService.getByEmail(email);
        // Checked after the lookup: the id is what the ownership rule needs. A caller who
        // may not see this user gets 403, so the endpoint cannot be used to test which
        // email addresses exist.
        authorization.assertCanAccessUser(user.userId());
        return user;
    }

    @GetMapping("/role/{roleId}") @Override
    public List<UserResponse> getByRoleId(@PathVariable Long roleId) {
        authorization.assertManager();
        return userService.getByRoleId(roleId);
    }

    @GetMapping("/manager/{managerId}") @Override
    public List<UserResponse> getByManagerId(@PathVariable Long managerId) {
        // assertCanAccessUser allows a manager to list their own reports (managerId == self)
        // and blocks one manager from browsing another manager's team.
        authorization.assertCanAccessUser(managerId);
        return userService.getByManagerId(managerId);
    }

    @PutMapping("/{id}") @Override
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        authorization.assertManager();
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Override
    public void delete(@PathVariable Long id) {
        authorization.assertManager();
        userService.delete(id);
    }
}
