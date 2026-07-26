package com.example.usermicroservice.service;

import com.example.usermicroservice.dto.request.CreateUserRequest;
import com.example.usermicroservice.dto.response.UserResponse;

import java.util.List;

public interface IUserService {
    UserResponse create(CreateUserRequest request);
    UserResponse getById(Long id);
    UserResponse getByEmail(String email);
    List<UserResponse> getAll();
    List<UserResponse> getByRoleId(Long roleId);
    List<UserResponse> getByManagerId(Long managerId);
    UserResponse update(Long id, CreateUserRequest request);
    void delete(Long id);
}
