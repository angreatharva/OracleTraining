package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateUserRequest;
import com.example.usermicroservice.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserController {
    ResponseEntity<UserResponse> create(CreateUserRequest request);
    List<UserResponse> getAll();
    UserResponse getById(Long id);
    UserResponse getByEmail(String email);
    List<UserResponse> getByRoleId(Long roleId);
    List<UserResponse> getByManagerId(Long managerId);
    UserResponse update(Long id, CreateUserRequest request);
    void delete(Long id);
}
