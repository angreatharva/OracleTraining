package com.example.usermicroservice.controllers;

import com.example.usermicroservice.dto.request.CreateRoleRequest;
import com.example.usermicroservice.dto.response.RoleResponse;
import com.example.usermicroservice.security.AuthorizationHelper;
import com.example.usermicroservice.service.IRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Roles are reference data: any authenticated user may read them (the frontend needs role
 * names to label users), but only a MANAGER may change the set of roles.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController implements IRoleController {
    private final IRoleService roleService;
    private final AuthorizationHelper authorization;

    public RoleController(IRoleService roleService, AuthorizationHelper authorization) {
        this.roleService = roleService;
        this.authorization = authorization;
    }

    @PostMapping @Override
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        authorization.assertManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @GetMapping @Override public List<RoleResponse> getAll() { return roleService.getAll(); }

    @GetMapping("/{id}") @Override public RoleResponse getById(@PathVariable Long id) { return roleService.getById(id); }

    @GetMapping("/name/{roleName}") @Override public RoleResponse getByRoleName(@PathVariable String roleName) { return roleService.getByRoleName(roleName); }

    @PutMapping("/{id}") @Override
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest request) {
        authorization.assertManager();
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Override
    public void delete(@PathVariable Long id) {
        authorization.assertManager();
        roleService.delete(id);
    }
}
