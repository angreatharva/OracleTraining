package com.example.usermicroservice.service;

import com.example.usermicroservice.dto.request.CreateRoleRequest;
import com.example.usermicroservice.dto.response.RoleResponse;
import com.example.usermicroservice.entities.Role;
import com.example.usermicroservice.exceptions.ResourceNotFoundException;
import com.example.usermicroservice.repositories.RoleRepository;
import com.example.usermicroservice.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleService implements IRoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByRoleName(request.roleName())) {
            throw new IllegalArgumentException("Role name already exists: " + request.roleName());
        }
        return toResponse(roleRepository.save(Role.builder().roleName(request.roleName()).build()));
    }

    @Override @Transactional(readOnly = true)
    public RoleResponse getById(Long id) { return toResponse(getEntityById(id)); }

    @Override @Transactional(readOnly = true)
    public RoleResponse getByRoleName(String roleName) {
        return toResponse(roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleName)));
    }

    @Override @Transactional(readOnly = true)
    public List<RoleResponse> getAll() { return roleRepository.findAll().stream().map(this::toResponse).toList(); }

    @Override
    public RoleResponse update(Long id, CreateRoleRequest request) {
        Role role = getEntityById(id);
        if (!role.getRoleName().equalsIgnoreCase(request.roleName()) && roleRepository.existsByRoleName(request.roleName())) {
            throw new IllegalArgumentException("Role name already exists: " + request.roleName());
        }
        role.setRoleName(request.roleName());
        return toResponse(roleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        Role role = getEntityById(id);
        if (userRepository.existsByRole(role)) throw new IllegalStateException("Role is assigned to one or more users");
        roleRepository.delete(role);
    }

    private Role getEntityById(Long id) { return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role", id)); }
    private RoleResponse toResponse(Role role) { return new RoleResponse(role.getRoleId(), role.getRoleName()); }
}
