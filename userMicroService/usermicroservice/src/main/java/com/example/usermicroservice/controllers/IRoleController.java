package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateRoleRequest;
import com.example.usermicroservice.dto.response.RoleResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface IRoleController {
    ResponseEntity<RoleResponse> create(CreateRoleRequest request);
    List<RoleResponse> getAll();
    RoleResponse getById(Long id);
    RoleResponse getByRoleName(String roleName);
    RoleResponse update(Long id, CreateRoleRequest request);
    void delete(Long id);
}
