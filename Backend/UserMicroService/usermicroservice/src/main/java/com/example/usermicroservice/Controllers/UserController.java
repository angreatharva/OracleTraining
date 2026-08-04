package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateUserRequest;
import com.example.usermicroservice.dto.response.UserResponse;
import com.example.usermicroservice.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController implements IUserController {
    private final IUserService userService;
    public UserController(IUserService userService) { this.userService = userService; }

    @PostMapping @Override
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request)); }
    @GetMapping @Override public List<UserResponse> getAll() { return userService.getAll(); }
    @GetMapping("/{id}") @Override public UserResponse getById(@PathVariable Long id) { return userService.getById(id); }
    @GetMapping("/email/{email}") @Override public UserResponse getByEmail(@PathVariable String email) { return userService.getByEmail(email); }
    @GetMapping("/role/{roleId}") @Override public List<UserResponse> getByRoleId(@PathVariable Long roleId) { return userService.getByRoleId(roleId); }
    @GetMapping("/manager/{managerId}") @Override public List<UserResponse> getByManagerId(@PathVariable Long managerId) { return userService.getByManagerId(managerId); }
    @PutMapping("/{id}") @Override public UserResponse update(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) { return userService.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Override public void delete(@PathVariable Long id) { userService.delete(id); }
}
