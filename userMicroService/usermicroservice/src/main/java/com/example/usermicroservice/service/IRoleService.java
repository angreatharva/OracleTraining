package com.example.usermicroservice.service;

import com.example.usermicroservice.dto.request.CreateRoleRequest;
import com.example.usermicroservice.dto.response.RoleResponse;

import java.util.List;

public interface IRoleService {
    RoleResponse create(CreateRoleRequest request);
    RoleResponse getById(Long id);
    RoleResponse getByRoleName(String roleName);
    List<RoleResponse> getAll();
    RoleResponse update(Long id, CreateRoleRequest request);
    void delete(Long id);
}
